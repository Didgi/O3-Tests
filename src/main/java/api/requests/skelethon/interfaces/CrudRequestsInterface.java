package api.requests.skelethon.interfaces;

import api.models.BaseModel;

import java.util.UUID;

public interface CrudRequestsInterface {
    Object POST(BaseModel baseModel);

    Object GET();

    Object PUT(BaseModel baseModel);

    Object PATCH(BaseModel baseModel);

    Object DELETE(UUID id);
}
