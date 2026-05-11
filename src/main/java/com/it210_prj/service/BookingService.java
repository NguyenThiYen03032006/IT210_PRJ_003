package com.it210_prj.service;

import com.it210_prj.model.dto.BookingHistoryDTO;
import com.it210_prj.model.dto.BookingInvoiceDetailDTO;
import com.it210_prj.model.dto.BookingResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {

    /** Đặt vé: kiểm tra suất còn hiệu lực, ghế thuộc phòng và chưa bán, tạo booking PAID và các ticket. */
    BookingResponse bookTickets(String userEmail, Long showtimeId, List<Long> seatIds);

    /** Khách gửi yêu cầu hủy (chỉ khi đơn PAID); không kiểm tra khoảng cách 24h tại đây. */
    void requestCancelBooking(String userEmail, Long bookingId);

    /** Gom vé theo booking để hiển thị lịch sử khách hàng. */
    List<BookingHistoryDTO> getHistory(String userEmail);

    /** Chi tiết một hóa đơn nếu đúng chủ (email). */
    BookingInvoiceDetailDTO getBookingInvoiceDetail(String userEmail, Long bookingId);

    /** Danh sách đặt vé cho nhân viên: phân trang; q rỗng = tất cả, số = theo mã đơn, khác = tìm email. */
    Page<BookingHistoryDTO> getStaffHistory(String keyword, int page, int size);

    /** Nhân viên xác nhận đã in vé: chuyển trạng thái sang CONFIRMED. */
    void confirmBooking(Long bookingId);

    /** Nhân viên hủy và giải phóng ghế; chỉ khi chưa CONFIRMED và còn ≥24h trước giờ chiếu. */
    void cancelBookingByStaff(Long bookingId);
}
