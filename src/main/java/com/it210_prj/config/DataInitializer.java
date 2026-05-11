package com.it210_prj.config;

import com.it210_prj.model.entity.Category;
import com.it210_prj.model.entity.Genre;
import com.it210_prj.model.entity.Movie;
import com.it210_prj.model.entity.Role;
import com.it210_prj.model.entity.Room;
import com.it210_prj.model.entity.Seat;
import com.it210_prj.model.entity.Showtime;
import com.it210_prj.model.entity.User;
import com.it210_prj.model.entity.UserProfile;
import com.it210_prj.repository.BookingRepository;
import com.it210_prj.repository.CategoryRepository;
import com.it210_prj.repository.GenreRepository;
import com.it210_prj.repository.MovieRepository;
import com.it210_prj.repository.RoomRepository;
import com.it210_prj.repository.SeatRepository;
import com.it210_prj.repository.ShowtimeRepository;
import com.it210_prj.repository.TicketRepository;
import com.it210_prj.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    private static final LocalDateTime SHOW_PAST_R1_MORNING =
            LocalDateTime.of(2026, 5, 1, 9, 0);
    private static final LocalDateTime SHOW_PAST_R1_EVENING =
            LocalDateTime.of(2026, 5, 2, 18, 30);
    private static final LocalDateTime SHOW_PAST_R2_AFTERNOON =
            LocalDateTime.of(2026, 5, 3, 14, 0);
    private static final LocalDateTime SHOW_PAST_R2_LATE =
            LocalDateTime.of(2026, 5, 4, 20, 15);

    private static final LocalDateTime SHOW_FUTURE_R1_2D =
            LocalDateTime.of(2026, 6, 10, 9, 0);
    private static final LocalDateTime SHOW_FUTURE_R1_3D =
            LocalDateTime.of(2026, 6, 10, 13, 0);
    private static final LocalDateTime SHOW_FUTURE_R2_DRAMA =
            LocalDateTime.of(2026, 6, 10, 11, 0);
    private static final LocalDateTime SHOW_FUTURE_R2_ANIM =
            LocalDateTime.of(2026, 6, 10, 16, 0);

    @Bean
    CommandLineRunner initData(
            UserRepository userRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            CategoryRepository categoryRepository,
            GenreRepository genreRepository,
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            SeatRepository seatRepository,
            ShowtimeRepository showtimeRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder encoder
    ) {
        return args -> {
            resetTestData(jdbcTemplate);
            ensureSeedSchema(jdbcTemplate);

            seedUsers(userRepository, encoder);
            SeedCatalog catalog = seedCatalog(categoryRepository, genreRepository, movieRepository);
            SeedRooms rooms = seedRooms(roomRepository, seatRepository);
            seedShowtimes(showtimeRepository, catalog, rooms);
        };
    }

    private void resetTestData(JdbcTemplate jdbcTemplate) {
        String[] tables = {
                "tickets",
                "ticket",
                "bookings",
                "booking",
                "showtimes",
                "showtime",
                "seats",
                "seat",
                "movies",
                "movie",
                "rooms",
                "room",
                "user_profile",
                "user_profiles",
                "users",
                "user",
                "categories",
                "category",
                "genres",
                "genre"
        };

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (String table : tables) {
            if (tableExists(jdbcTemplate, table)) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            }
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void ensureSeedSchema(JdbcTemplate jdbcTemplate) {
        if (tableExists(jdbcTemplate, "rooms") && !columnExists(jdbcTemplate, "rooms", "total_seats")) {
            jdbcTemplate.execute("ALTER TABLE rooms ADD COLUMN total_seats INT NULL");
        }
    }

    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                AND table_name = ?
                AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private void seedUsers(UserRepository userRepository, PasswordEncoder encoder) {
        createUser(userRepository, encoder, "admin", "admin@gmail.com", Role.ADMIN, "Admin System", "0900000001");
        createUser(userRepository, encoder, "staff", "staff@gmail.com", Role.STAFF, "Staff User", "0900000002");
        createUser(userRepository, encoder, "user", "user@gmail.com", Role.CUSTOMER, "Nguyen Van A", "0900000003");
    }

    private void createUser(
            UserRepository userRepository,
            PasswordEncoder encoder,
            String username,
            String email,
            Role role,
            String fullName,
            String phone
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode("123456"));
        user.setRole(role);

        UserProfile profile = new UserProfile();
        profile.setFullName(fullName);
        profile.setPhone(phone);
        profile.setUser(user);
        user.setProfile(profile);

        userRepository.save(user);
    }

    private SeedCatalog seedCatalog(
            CategoryRepository categoryRepository,
            GenreRepository genreRepository,
            MovieRepository movieRepository
    ) {
        Category nowShowing = new Category();
        nowShowing.setName("Now Showing");
        categoryRepository.save(nowShowing);

        Category comingSoon = new Category();
        comingSoon.setName("Coming Soon");
        categoryRepository.save(comingSoon);

        Genre action = new Genre();
        action.setName("Action");
        genreRepository.save(action);

        Genre drama = new Genre();
        drama.setName("Drama");
        genreRepository.save(drama);

        Genre animation = new Genre();
        animation.setName("Animation");
        genreRepository.save(animation);

        Movie goldenNight = createMovie(
                movieRepository,
                "Golden Night",
                "A stylish action blockbuster about a last mission inside a neon city.",
                120,
                "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=700&q=80",
                nowShowing,
                action,
                "T16",
                "Jordan Lee",
                "https://www.youtube.com/watch?v=TnGl01FkMMo",
                true
        );

        Movie silentOrbit = createMovie(
                movieRepository,
                "Silent Orbit",
                "A space drama about a crew trying to return home before the final signal fades.",
                105,
                "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=700&q=80",
                nowShowing,
                drama,
                "T13",
                "Mai Anh",
                "https://www.youtube.com/watch?v=TnGl01FkMMo",
                true
        );

        Movie littleComet = createMovie(
                movieRepository,
                "Little Comet",
                "A warm animated adventure for families and young audiences.",
                95,
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=700&q=80",
                comingSoon,
                animation,
                "P",
                "Studio North",
                "https://www.youtube.com/watch?v=TnGl01FkMMo",
                false
        );

        return new SeedCatalog(goldenNight, silentOrbit, littleComet);
    }

    private Movie createMovie(
            MovieRepository movieRepository,
            String title,
            String description,
            int duration,
            String poster,
            Category category,
            Genre genre,
            String ageRating,
            String director,
            String trailerUrl,
            boolean hot
    ) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setReleaseDate(LocalDate.now());
        movie.setPoster(poster);
        movie.setCategory(category);
        movie.setGenre(genre);
        movie.setAgeRating(ageRating);
        movie.setDirector(director);
        movie.setTrailerUrl(trailerUrl);
        movie.setHot(hot);
        return movieRepository.save(movie);
    }

    private SeedRooms seedRooms(RoomRepository roomRepository, SeatRepository seatRepository) {
        Room room1 = createRoom(roomRepository, seatRepository, "Room 1", 50);
        Room room2 = createRoom(roomRepository, seatRepository, "Room 2", 50);
        return new SeedRooms(room1, room2);
    }

    private Room createRoom(
            RoomRepository roomRepository,
            SeatRepository seatRepository,
            String name,
            int totalSeats
    ) {
        Room room = new Room();
        room.setName(name);
        room.setTotalSeats(totalSeats);
        roomRepository.save(room);

        for (char row = 'A'; row <= 'E'; row++) {
            String seatType = row <= 'B' ? "STANDARD" : row <= 'D' ? "VIP" : "COUPLE";
            for (int number = 0; number <= 9; number++) {
                Seat seat = new Seat();
                seat.setRoom(room);
                seat.setSeatName(row + String.valueOf(number));
                seat.setSeatType(seatType);
                seatRepository.save(seat);
            }
        }

        return room;
    }

    private void seedShowtimes(
            ShowtimeRepository showtimeRepository,
            SeedCatalog catalog,
            SeedRooms rooms
    ) {
        // Đã chiếu (start < “hiện tại” khi demo quanh 5–6/2026) — dùng test ẩn suất / lịch sử
        createShowtime(
                showtimeRepository,
                catalog.goldenNight(),
                rooms.room1(),
                SHOW_PAST_R1_MORNING,
                "2D",
                "EXPIRED"
        );
        createShowtime(
                showtimeRepository,
                catalog.goldenNight(),
                rooms.room1(),
                SHOW_PAST_R1_EVENING,
                "3D",
                "EXPIRED"
        );
        createShowtime(
                showtimeRepository,
                catalog.silentOrbit(),
                rooms.room2(),
                SHOW_PAST_R2_AFTERNOON,
                "2D",
                "EXPIRED"
        );
        createShowtime(
                showtimeRepository,
                catalog.littleComet(),
                rooms.room2(),
                SHOW_PAST_R2_LATE,
                "2D",
                "EXPIRED"
        );

        // Sắp chiếu — dùng test đặt vé / full phòng
        createShowtime(
                showtimeRepository,
                catalog.goldenNight(),
                rooms.room1(),
                SHOW_FUTURE_R1_2D,
                "2D",
                "ACTIVE"
        );
        createShowtime(
                showtimeRepository,
                catalog.goldenNight(),
                rooms.room1(),
                SHOW_FUTURE_R1_3D,
                "3D",
                "ACTIVE"
        );
        createShowtime(
                showtimeRepository,
                catalog.silentOrbit(),
                rooms.room2(),
                SHOW_FUTURE_R2_DRAMA,
                "2D",
                "ACTIVE"
        );
        createShowtime(
                showtimeRepository,
                catalog.littleComet(),
                rooms.room2(),
                SHOW_FUTURE_R2_ANIM,
                "2D",
                "ACTIVE"
        );
    }

    private void createShowtime(
            ShowtimeRepository showtimeRepository,
            Movie movie,
            Room room,
            LocalDateTime startTime,
            String screenFormat,
            String status
    ) {
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(startTime.plusMinutes(movie.getDuration()));
        showtime.setStatus(status != null && !status.isBlank() ? status : "ACTIVE");
        showtime.setScreenFormat(screenFormat != null ? screenFormat : "2D");
        showtimeRepository.save(showtime);
    }

    private record SeedCatalog(Movie goldenNight, Movie silentOrbit, Movie littleComet) {
    }

    private record SeedRooms(Room room1, Room room2) {
    }
}
