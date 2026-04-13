package com.example.gotix.server;

import android.content.Context;
import com.example.gotix.model.Event;
import com.example.gotix.model.Reservation;
import com.example.gotix.model.User;
import com.example.gotix.service.EventService;
import com.example.gotix.service.ReservationService;
import com.example.gotix.service.UserService;
import com.example.gotix.service.ValidationService;
import com.example.gotix.util.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalApiServer extends NanoHTTPD {
    private final Context context;
    private final EventService eventService = new EventService();
    private final UserService userService = new UserService();
    private final ReservationService reservationService = new ReservationService();
    private final ValidationService validationService = new ValidationService();

    public LocalApiServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // ── Static assets ────────────────────────────────────────────────────
        if (Method.GET.equals(method)) {
            if (uri.equals("/") || uri.equals("/index.html")) {
                return serveStaticFile("index.html", "text/html");
            } else if (uri.equals("/styles.css")) {
                return serveStaticFile("styles.css", "text/css");
            } else if (uri.equals("/app.js")) {
                return serveStaticFile("app.js", "application/javascript");
            }
        }

        // ── Events ───────────────────────────────────────────────────────────
        if (Method.GET.equals(method) && uri.equals("/events")) {
            List<Event> events = eventService.getAllEvents();
            return json(JsonUtil.toJson(events));
        }
        if (Method.GET.equals(method) && uri.equals("/events/search")) {
            Map<String, String> params = session.getParms();
            List<Event> filtered = eventService.searchEvents(
                    params.get("date"), params.get("location"), params.get("category"));
            return json(JsonUtil.toJson(filtered));
        }

        // ── User registration ────────────────────────────────────────────────
        if (Method.POST.equals(method) && uri.equals("/users/register")) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String body = files.get("postData");
                User req = JsonUtil.fromJson(body, User.class);
                if (!validationService.isValidRegistration(req.getEmail(), req.getPhone())) {
                    return jsonError(Response.Status.BAD_REQUEST, "Email or phone required");
                }
                User user = userService.registerUser(req.getEmail(), req.getPhone());
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "ok");
                resp.put("userId", user.getId());
                return json(JsonUtil.toJson(resp));
            } catch (IOException | ResponseException e) {
                return jsonError(Response.Status.INTERNAL_ERROR, e.getMessage());
            }
        }

        // ── Reservations ─────────────────────────────────────────────────────
        // POST /reservations  — create a reservation
        if (Method.POST.equals(method) && uri.equals("/reservations")) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String body = files.get("postData");
                Map<String, Object> req = JsonUtil.fromJson(body, Map.class);

                String userId  = (String) req.get("userId");
                String eventId = (String) req.get("eventId");
                int numTickets = req.containsKey("numTickets")
                        ? ((Number) req.get("numTickets")).intValue() : 1;

                if (userId == null || eventId == null) {
                    return jsonError(Response.Status.BAD_REQUEST, "userId and eventId are required");
                }

                Reservation reservation = reservationService.reserve(userId, eventId, numTickets);
                if (reservation == null) {
                    return jsonError(Response.Status.BAD_REQUEST, "Not enough seats available");
                }
                Map<String, Object> resp = new HashMap<>();
                resp.put("status", "ok");
                resp.put("reservationId", reservation.getId());
                resp.put("eventTitle", reservation.getEventTitle());
                resp.put("numTickets", reservation.getNumTickets());
                resp.put("reservationStatus", reservation.getStatus());
                return json(JsonUtil.toJson(resp));
            } catch (IOException | ResponseException e) {
                return jsonError(Response.Status.INTERNAL_ERROR, e.getMessage());
            }
        }

        // GET /reservations?userId=  — list reservations for a user
        if (Method.GET.equals(method) && uri.equals("/reservations")) {
            String userId = session.getParms().get("userId");
            if (userId == null) {
                return jsonError(Response.Status.BAD_REQUEST, "userId param required");
            }
            List<Reservation> list = reservationService.getByUser(userId);
            return json(JsonUtil.toJson(list));
        }

        // DELETE /reservations/{id}  — cancel a reservation
        if (Method.DELETE.equals(method) && uri.startsWith("/reservations/")) {
            String reservationId = uri.substring("/reservations/".length());
            boolean cancelled = reservationService.cancel(reservationId);
            Map<String, Object> resp = new HashMap<>();
            if (cancelled) {
                resp.put("status", "ok");
                resp.put("message", "Reservation cancelled");
                return json(JsonUtil.toJson(resp));
            } else {
                return jsonError(Response.Status.BAD_REQUEST, "Reservation not found or already cancelled");
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    private Response json(String body) {
        return newFixedLengthResponse(Response.Status.OK, "application/json", body);
    }

    private Response jsonError(Response.Status status, String message) {
        return newFixedLengthResponse(status, "application/json",
                "{\"error\": \"" + message.replace("\"", "'") + "\"}");
    }

    private Response serveStaticFile(String fileName, String mimeType) {
        try {
            InputStream is = context.getAssets().open(fileName);
            return newFixedLengthResponse(Response.Status.OK, mimeType, is, is.available());
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found: " + fileName);
        }
    }
}
