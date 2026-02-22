package com.example.gotix.server;

import android.content.Context;
import android.content.res.AssetManager;
import com.example.gotix.model.Event;
import com.example.gotix.model.User;
import com.example.gotix.service.EventService;
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
    private Context context;
    private EventService eventService = new EventService();
    private UserService userService = new UserService();
    private ValidationService validationService = new ValidationService();

    public LocalApiServer(Context context, int port) {
        super(port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.GET.equals(method)) {
            if (uri.equals("/") || uri.equals("/index.html")) {
                return serveStaticFile("index.html", "text/html");
            } else if (uri.equals("/styles.css")) {
                return serveStaticFile("styles.css", "text/css");
            } else if (uri.equals("/app.js")) {
                return serveStaticFile("app.js", "application/javascript");
            } else if (uri.equals("/events")) {
                List<Event> events = eventService.getAllEvents();
                return newFixedLengthResponse(Response.Status.OK, "application/json", JsonUtil.toJson(events));
            } else if (uri.equals("/events/search")) {
                Map<String, String> params = session.getParms();
                String date = params.get("date");
                String location = params.get("location");
                String category = params.get("category");
                List<Event> filtered = eventService.searchEvents(date, location, category);
                return newFixedLengthResponse(Response.Status.OK, "application/json", JsonUtil.toJson(filtered));
            }
        } else if (Method.POST.equals(method)) {
            if (uri.equals("/users/register")) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String postData = files.get("postData");
                    User registrationRequest = JsonUtil.fromJson(postData, User.class);

                    if (!validationService.isValidRegistration(registrationRequest.getEmail(), registrationRequest.getPhone())) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\": \"Email or phone required\"}");
                    }

                    User user = userService.registerUser(registrationRequest.getEmail(), registrationRequest.getPhone());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "ok");
                    response.put("userId", user.getId());
                    response.put("message", "Registered");
                    return newFixedLengthResponse(Response.Status.OK, "application/json", JsonUtil.toJson(response));
                } catch (IOException | ResponseException e) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error: " + e.getMessage());
                }
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    private Response serveStaticFile(String fileName, String mimeType) {
        try {
            InputStream is = context.getAssets().open(fileName);
            return newFixedLengthResponse(Response.Status.OK, mimeType, is, is.available());
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
        }
    }
}
