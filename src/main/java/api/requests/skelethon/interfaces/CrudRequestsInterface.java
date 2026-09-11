package api.requests.skelethon.interfaces;

import api.models.BaseModel;

public interface CrudRequestsInterface {
    Object POST(BaseModel baseModel);

    Object GET();

    Object PUT(BaseModel baseModel);

    Object PATCH(BaseModel baseModel);

    Object DELETE(int id);
}
