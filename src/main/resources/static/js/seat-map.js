document.addEventListener("DOMContentLoaded", function () {
    const selectedSeats = new Set();
    const selectedList = document.querySelector("[data-selected-seats]");
    const totalPrice = document.querySelector("[data-total-price]");
    const bookingForm = document.querySelector("[data-booking-form]");
    const messageBox = document.querySelector("[data-booking-message]");
    const pricePerSeat = Number(document.body.dataset.ticketPrice || 75000);

    function money(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + "đ";
    }

    function renderSummary() {
        const labels = Array.from(selectedSeats).map(function (seatId) {
            const seat = document.querySelector("[data-seat-id='" + seatId + "']");
            return seat ? seat.dataset.seatName : seatId;
        });
        selectedList.textContent = labels.length ? labels.join(", ") : "Chưa chọn";
        totalPrice.textContent = money(labels.length * pricePerSeat);
    }

    document.querySelectorAll("[data-seat-id]").forEach(function (seat) {
        seat.addEventListener("click", function () {
            if (seat.classList.contains("sold")) return;
            const seatId = seat.dataset.seatId;
            if (selectedSeats.has(seatId)) {
                selectedSeats.delete(seatId);
                seat.classList.remove("selected");
            } else {
                selectedSeats.add(seatId);
                seat.classList.add("selected");
            }
            renderSummary();
        });
    });

    if (bookingForm) {
        bookingForm.addEventListener("submit", function (event) {
            event.preventDefault();
            if (!selectedSeats.size) {
                messageBox.textContent = "Vui lòng chọn ít nhất một ghế.";
                return;
            }

            fetch("/customer/bookings", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    showtimeId: Number(bookingForm.dataset.showtimeId),
                    seatIds: Array.from(selectedSeats).map(Number)
                })
            })
                .then(function (response) {
                    if (!response.ok) {
                        return response.text().then(function (text) {
                            throw new Error(text || "Không thể đặt vé.");
                        });
                    }
                    return response.json();
                })
                .then(function () {
                    window.location.href = "/customer/bookings";
                })
                .catch(function (error) {
                    messageBox.textContent = error.message;
                });
        });
    }

    renderSummary();
});
