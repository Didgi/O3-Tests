package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static api.config.ApiPath.*;

@AllArgsConstructor
@Getter
public enum EndpointRequests {
    GET_SESSION(GET_SESSION_PATH, null, GetSessionResponse.class);

    private final String path;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
