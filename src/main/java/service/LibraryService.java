package service;

import model.Book;
import model.BorrowRecord;
import model.Member;
import repository.BookRepository;
import repository.BorrowRepository;
import repository.MemberRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LibraryService {
    private final BookRepository bookRepository = new BookRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final BorrowRepository borrowRepository = new BorrowRepository();

    public boolean authenticate(String username, String password) {
        return "admin".equals(username.trim()) && "1234".equals(password);
    }

    public void addBook(Book book) throws SQLException {
        if (book.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        book.setAvailableQuantity(book.getQuantity());
        bookRepository.save(book);
    }

    public void addMember(Member member) throws SQLException {
        memberRepository.save(member);
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
        memberRepository.update(member);
    }

    public void deleteMember(String memberCode) throws SQLException {
        memberRepository.deleteByCode(memberCode);
    }

    public List<Member> getMembersForDropdown() throws SQLException {
        return memberRepository.findAll();
    }

    public List<Book> getAvailableBooksForDropdown() throws SQLException {
        return bookRepository.findAvailableBooks();
    }

    public void issueBook(Member member, Book book, LocalDate issueDate, LocalDate dueDate) throws SQLException {
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date.");
        }
        boolean decremented = bookRepository.updateAvailableQuantity(book.getBookCode(), -1);
        if (!decremented) {
            throw new IllegalStateException("No copies available for this book.");
        }

        BorrowRecord record = new BorrowRecord();
        record.setMemberCode(member.getMemberCode());
        record.setMemberName(member.getFullName());
        record.setBookCode(book.getBookCode());
        record.setBookTitle(book.getTitle());
        record.setIssueDate(issueDate);
        record.setDueDate(dueDate);
        borrowRepository.saveIssue(record);
    }

    public List<BorrowRecord> getActiveBorrowedRecords() throws SQLException {
        return borrowRepository.findActiveBorrowedRecords();
    }

    public String returnBook(BorrowRecord record, LocalDate returnDate) throws SQLException {
        if (returnDate.isBefore(record.getIssueDate())) {
            throw new IllegalArgumentException("Return date cannot be before issue date.");
        }
        String finalStatus = returnDate.isAfter(record.getDueDate()) ? "Overdue" : "Returned";
        borrowRepository.markReturned(record.getId(), returnDate, finalStatus);
        bookRepository.updateAvailableQuantity(record.getBookCode(), 1);
        return finalStatus;
    }

    public List<BorrowRecord> getBorrowingHistory() throws SQLException {
        return borrowRepository.findAllHistory();
    }

    public int getTotalBooks() throws SQLException {
        return bookRepository.getTotalBookCount();
    }

    public int getTotalMembers() throws SQLException {
        return memberRepository.getTotalMemberCount();
    }

    public int getCurrentlyBorrowedCount() throws SQLException {
        return borrowRepository.countCurrentlyBorrowed();
    }

    public int getOverdueCount() throws SQLException {
        return borrowRepository.countOverdue();
    }
}