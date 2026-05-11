package com.it210_prj.service;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingInvoiceDetailDTO;
import com.it210_prj.model.dto.BookingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {

    // Đặt vé
    BookingResponse bookTickets(String userEmail, Long showtimeId, List<Long> seatIds);

    // Khách gửi yêu cầu hủy đơn
    void requestCancelBooking(String userEmail, Long bookingId);

    // Lấy lịch sử đặt vé của khách
    List<BookingHistoryDTO> getHistory(String userEmail);

    // Lấy chi tiết hóa đơn
    BookingInvoiceDetailDTO getBookingInvoiceDetail(String userEmail, Long bookingId);

    // Lấy danh sách đặt vé cho nhân viên (có phân trang)
    Page<BookingHistoryDTO> getStaffHistory(String keyword, int page, int size);

    // Nhân viên xác nhận đã in vé
    // Chuyển trạng thái booking sang CONFIRMED
    void confirmBooking(Long bookingId);

    // Nhân viên hủy booking
    void cancelBookingByStaff(Long bookingId);
}