package service;

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

    public int getTotalBooks() throws SQLException {
        return bookRepository.getTotalBooksCount();
    }

    public int getTotalMembers() throws SQLException {
        return memberRepository.getTotalMembersCount();
    }

    public int getCurrentlyBorrowedCount() throws SQLException {
        return borrowRepository.getCurrentlyBorrowedCount();
    }

    public int getOverdueCount() throws SQLException {
        return borrowRepository.getOverdueCount(LocalDate.now(ZoneId.systemDefault()));
    }

    public void addBook(Book book) throws SQLException {
        if (book == null) {
            throw new IllegalArgumentException("Book details cannot be null.");
        }
        bookRepository.save(book);
    }

    public Book findBookByCode(String bookCode) throws SQLException {
        return bookRepository.findByCode(bookCode);
    }

    public void addMember(Member member) throws SQLException {
        if (member == null) {
            throw new IllegalArgumentException("Member details cannot be null.");
        }
        memberRepository.save(member);
    }

    public Member findMemberByCode(String memberCode) throws SQLException {
        return memberRepository.findByCode(memberCode);
    }

    public List<Member> getAllMembers() throws SQLException {
        return memberRepository.findAll();
    }

    public List<Member> searchMembers(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return memberRepository.findAll();
        }
        return memberRepository.search(keyword.trim());
    }

    public void updateMember(Member member) throws SQLException {
        if (member == null) {
            throw new IllegalArgumentException("Member details cannot be null.");
        }
        memberRepository.update(member);
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

        borrowRepository.issueBook(memberCode, bookCode, issueDate, dueDate);
        bookRepository.decrementAvailableQuantity(bookCode);
    }

    public List<BorrowRecord> getActiveBorrowRecords() throws SQLException {
        return borrowRepository.findActiveRecords();
    }

    public List<BorrowRecord> searchActiveBorrowRecords(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return borrowRepository.findActiveRecords();
        }
        return borrowRepository.searchActiveRecords(keyword.trim());
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

    public List<BorrowRecord> getBorrowHistory(String status, String keyword) throws SQLException {
        List<BorrowRecord> allRecords = borrowRepository.findAllHistory();
        String normalizedStatus = (status == null || status.isBlank()) ? FILTER_ALL : status.trim();
        String normalizedKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<BorrowRecord> filtered = new ArrayList<>();
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
                filtered.add(borrowRecord);
            }
        }
        return filtered;
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }
}