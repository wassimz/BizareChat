package com.internship.pbt.bizarechat.data.repository;


import com.internship.pbt.bizarechat.data.datamodel.response.AllDialogsResponse;
import com.internship.pbt.bizarechat.data.datamodel.response.DialogUpdateResponseModel;
import com.internship.pbt.bizarechat.data.net.requests.dialog.DialogUpdateRequestModel;
import com.internship.pbt.bizarechat.data.net.services.DialogsService;
import com.internship.pbt.bizarechat.domain.repository.DialogsRepository;

import java.util.HashMap;

import rx.Observable;

public class DialogsDataRepository implements DialogsRepository{
    private DialogsService dialogsService;

    public DialogsDataRepository(DialogsService dialogsService){
        this.dialogsService = dialogsService;
    }

    @Override
    public Observable<AllDialogsResponse> getAllDialogs() {
        return dialogsService.getDialogs(UserToken.getInstance().getToken(), new HashMap<>());
    }

    @Override
    public Observable<DialogUpdateResponseModel> updateDialog(String dialogId, DialogUpdateRequestModel updateRequestModel) {
        return dialogsService.updateDialog(UserToken.getInstance().getToken(), dialogId, updateRequestModel);
    }
}
