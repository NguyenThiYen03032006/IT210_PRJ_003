document.addEventListener("DOMContentLoaded", function () {
    const selectedSeats = new Set();
    const selectedList = document.querySelector("[data-selected-seats]");
    const totalPrice = document.querySelector("[data-total-price]");
    const bookingForm = document.querySelector("[data-booking-form]");
    const messageBox = document.querySelector("[data-booking-message]");
    const prices = {
        STANDARD: Number(document.body.dataset.priceStandard || 75000),
        VIP: Number(document.body.dataset.priceVip || 95000),
        COUPLE: Number(document.body.dataset.priceCouple || 140000),
    };

    function money(value) {
        return new Intl.NumberFormat("vi-VN").format(value) + "d";
    }

    function priceForSeatElement(seat) {
        const t = (seat.dataset.seatType || "STANDARD").toUpperCase();
        return prices[t] != null ? prices[t] : prices.STANDARD;
    }

    function renderSummary() {
        const labels = [];
        let sum = 0;
        selectedSeats.forEach(function (seatId) {
            const seat = document.querySelector("[data-seat-id='" + seatId + "']");
            if (seat) {
                labels.push(seat.dataset.seatName);
                sum += priceForSeatElement(seat);
            }
        });
        selectedList.textContent = labels.length ? labels.join(", ") : "Chua chon";
        totalPrice.textContent = money(sum);
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
                messageBox.textContent = "Vui long chon it nhat mot ghe.";
                return;
            }

            fetch("/customer/bookings", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    showtimeId: Number(bookingForm.dataset.showtimeId),
                    seatIds: Array.from(selectedSeats).map(Number),
                }),
            })
                .then(function (response) {
                    if (response.status === 401 || response.status === 403) {
                        window.location.href = "/auth/login";
                        return Promise.reject(new Error("Can dang nhap de dat ve."));
                    }
                    if (!response.ok) {
                        return response.text().then(function (text) {
                            throw new Error(text || "Khong the dat ve.");
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
