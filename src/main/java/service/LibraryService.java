package service;

import dto.BookDTO;
import dto.BorrowRecordDTO;
import dto.DashboardStatsDTO;
import dto.MemberDTO;
import model.Book;
import model.BorrowRecord;
import model.Member;
import repository.BookRepository;
import repository.BorrowRepository;
import repository.MemberRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibraryService {

    private static final String FILTER_ALL = "All";

    private final BookRepository bookRepository = new BookRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final BorrowRepository borrowRepository = new BorrowRepository();

    public DashboardStatsDTO getDashboardStats() throws SQLException {
        int totalBooks = bookRepository.getTotalBooksCount();
        int totalMembers = memberRepository.getTotalMembersCount();
        int borrowed = borrowRepository.getCurrentlyBorrowedCount();
        int overdue = borrowRepository.getOverdueCount(LocalDate.now(ZoneId.systemDefault()));
        return new DashboardStatsDTO(totalBooks, totalMembers, borrowed, overdue);
    }

    public void addBook(BookDTO bookDto) throws SQLException {
        if (bookDto == null) {
            throw new IllegalArgumentException("Book details cannot be null.");
        }
        Book entity = toBookEntity(bookDto);
        bookRepository.save(entity);
    }

    public BookDTO findBookByCode(String bookCode) throws SQLException {
        Book book = bookRepository.findByCode(bookCode);
        return book != null ? toBookDTO(book) : null;
    }

    public void addMember(MemberDTO memberDto) throws SQLException {
        if (memberDto == null) {
            throw new IllegalArgumentException("Member details cannot be null.");
        }
        Member entity = toMemberEntity(memberDto);
        memberRepository.save(entity);
    }

    public MemberDTO findMemberByCode(String memberCode) throws SQLException {
        Member member = memberRepository.findByCode(memberCode);
        return member != null ? toMemberDTO(member) : null;
    }

    public List<MemberDTO> getAllMembers() throws SQLException {
        return toMemberDTOList(memberRepository.findAll());
    }

    public List<MemberDTO> searchMembers(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return getAllMembers();
        }
        return toMemberDTOList(memberRepository.search(keyword.trim()));
    }

    public void updateMember(MemberDTO memberDto) throws SQLException {
        if (memberDto == null) {
            throw new IllegalArgumentException("Member details cannot be null.");
        }
        memberRepository.update(toMemberEntity(memberDto));
    }

    public void deleteMember(String memberCode) throws SQLException {
        memberRepository.deleteByCode(memberCode);
    }

    public void issueBook(String memberCode, String bookCode, LocalDate issueDate, LocalDate dueDate)
            throws SQLException {
        Member member = memberRepository.findByCode(memberCode);
        if (member == null) {
            throw new IllegalArgumentException("Member not found with ID: " + memberCode);
        }

        Book book = bookRepository.findByCode(bookCode);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ID: " + bookCode);
        }

        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("No copies available for book: " + book.getTitle());
        }

        borrowRepository.issueBook(
                memberCode,
                member.getFullName(),
                bookCode,
                book.getTitle(),
                issueDate,
                dueDate
        );
        bookRepository.decrementAvailableQuantity(bookCode);
    }

    public List<BorrowRecordDTO> getActiveBorrowRecords() throws SQLException {
        return toBorrowRecordDTOList(borrowRepository.findActiveRecords());
    }

    public List<BorrowRecordDTO> searchActiveBorrowRecords(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return getActiveBorrowRecords();
        }
        return toBorrowRecordDTOList(borrowRepository.searchActiveRecords(keyword.trim()));
    }

    public void returnBook(int recordId, LocalDate returnDate) throws SQLException {
        BorrowRecord borrowRecord = borrowRepository.findById(recordId);
        if (borrowRecord == null) {
            throw new IllegalArgumentException("Borrow record not found with ID: " + recordId);
        }
        if ("RETURNED".equalsIgnoreCase(borrowRecord.getStatus())) {
            throw new IllegalStateException("This book has already been returned.");
        }

        borrowRepository.markAsReturned(recordId, returnDate);
        bookRepository.incrementAvailableQuantity(borrowRecord.getBookCode());
    }

    public List<BorrowRecordDTO> getBorrowHistory(String status, String keyword) throws SQLException {
        List<BorrowRecord> allRecords = borrowRepository.findAllHistory();
        String normalizedStatus = (status == null || status.isBlank()) ? FILTER_ALL : status.trim();
        String normalizedKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<BorrowRecordDTO> filtered = new ArrayList<>();
        for (BorrowRecord borrowRecord : allRecords) {
            boolean matchesStatus = FILTER_ALL.equalsIgnoreCase(normalizedStatus)
                    || (borrowRecord.getStatus() != null && borrowRecord.getStatus().equalsIgnoreCase(normalizedStatus));

            boolean matchesKeyword = normalizedKeyword.isEmpty()
                    || String.valueOf(borrowRecord.getId()).contains(normalizedKeyword)
                    || containsIgnoreCase(borrowRecord.getMemberCode(), normalizedKeyword)
                    || containsIgnoreCase(borrowRecord.getMemberName(), normalizedKeyword)
                    || containsIgnoreCase(borrowRecord.getBookCode(), normalizedKeyword)
                    || containsIgnoreCase(borrowRecord.getBookTitle(), normalizedKeyword)
                    || containsIgnoreCase(borrowRecord.getStatus(), normalizedKeyword);

            if (matchesStatus && matchesKeyword) {
                filtered.add(toBorrowRecordDTO(borrowRecord));
            }
        }
        return filtered;
    }

    // --- Entity <-> DTO Mappers ---

    private Book toBookEntity(BookDTO dto) {
        Book book = new Book(
                dto.getBookCode(),
                dto.getTitle(),
                dto.getAuthor(),
                dto.getCategory(),
                dto.getPublishedYear(),
                dto.getQuantity()
        );
        book.setId(dto.getId());
        if (dto.getAvailableQuantity() > 0) {
            book.setAvailableQuantity(dto.getAvailableQuantity());
        }
        return book;
    }

    private BookDTO toBookDTO(Book entity) {
        BookDTO dto = new BookDTO(
                entity.getBookCode(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getCategory(),
                entity.getPublishedYear(),
                entity.getQuantity()
        );
        dto.setId(entity.getId());
        dto.setAvailableQuantity(entity.getAvailableQuantity());
        return dto;
    }

    private Member toMemberEntity(MemberDTO dto) {
        Member member = new Member(
                dto.getMemberCode(),
                dto.getFullName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getAddress()
        );
        member.setId(dto.getId());
        return member;
    }

    private MemberDTO toMemberDTO(Member entity) {
        MemberDTO dto = new MemberDTO(
                entity.getMemberCode(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getAddress()
        );
        dto.setId(entity.getId());
        return dto;
    }

    private List<MemberDTO> toMemberDTOList(List<Member> entities) {
        List<MemberDTO> list = new ArrayList<>();
        for (Member member : entities) {
            list.add(toMemberDTO(member));
        }
        return list;
    }

    private BorrowRecordDTO toBorrowRecordDTO(BorrowRecord entity) {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(entity.getId());
        dto.setMemberCode(entity.getMemberCode());
        dto.setMemberName(entity.getMemberName());
        dto.setBookCode(entity.getBookCode());
        dto.setBookTitle(entity.getBookTitle());
        dto.setIssueDate(entity.getIssueDate());
        dto.setDueDate(entity.getDueDate());
        dto.setReturnDate(entity.getReturnDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private List<BorrowRecordDTO> toBorrowRecordDTOList(List<BorrowRecord> entities) {
        List<BorrowRecordDTO> list = new ArrayList<>();
        for (BorrowRecord borrowRecord : entities) {
            list.add(toBorrowRecordDTO(borrowRecord));
        }
        return list;
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }
}