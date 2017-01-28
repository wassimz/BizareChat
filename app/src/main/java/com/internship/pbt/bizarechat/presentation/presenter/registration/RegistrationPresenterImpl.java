package com.internship.pbt.bizarechat.presentation.presenter.registration;

import android.net.Uri;
import android.util.Log;

import com.facebook.login.LoginResult;
import com.internship.pbt.bizarechat.R;
import com.internship.pbt.bizarechat.data.net.ApiConstants;
import com.internship.pbt.bizarechat.data.net.requests.signup.SignUpRequestM;
import com.internship.pbt.bizarechat.data.net.requests.signup.SignUpUserM;
import com.internship.pbt.bizarechat.data.repository.ContentDataRepository;
import com.internship.pbt.bizarechat.data.repository.SessionDataRepository;
import com.internship.pbt.bizarechat.domain.interactor.SignUpUseCase;
import com.internship.pbt.bizarechat.domain.interactor.UploadFileUseCase;
import com.internship.pbt.bizarechat.domain.interactor.UseCase;
import com.internship.pbt.bizarechat.domain.model.signup.ResponseSignUpModel;
import com.internship.pbt.bizarechat.presentation.exception.ErrorMessageFactory;
import com.internship.pbt.bizarechat.presentation.model.CurrentUser;
import com.internship.pbt.bizarechat.presentation.model.FacebookLinkInform;
import com.internship.pbt.bizarechat.presentation.model.RegistrationModel;
import com.internship.pbt.bizarechat.presentation.model.SignUpModel;
import com.internship.pbt.bizarechat.presentation.util.Converter;
import com.internship.pbt.bizarechat.presentation.util.Validator;
import com.internship.pbt.bizarechat.presentation.view.fragment.register.RegistrationView;

import java.io.File;

import retrofit2.Response;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;
import rx.Subscriber;

public class RegistrationPresenterImpl implements RegistrationPresenter {

    private static final String PHONE_FORMAT = "+38 (0__) ___-__-__";
    private static final String USER_EXIST = "Sorry, this email already exist";
    private static final String AVATAR = "Too large picture max size 1mb";
    private final String TAG = "RegistrPresenterImpl";
    private Validator mValidator = new Validator();
    private RegistrationView mRegisterView;
    private File fileToUpload;
    private SignUpModel mRegistrationModel;
    private UseCase signUpUseCase;
    private SignUpRequestM signUpRequestM;

    public RegistrationPresenterImpl() {
        super();
        mRegistrationModel = new RegistrationModel();
        mRegistrationModel.setPresenter(this);
    }

    @Override
    public void setRegistrationView(RegistrationView registerView) {
        mRegisterView = registerView;
    }

    @Override
    public void showErrorInvalidPassword() {
        mRegisterView.showErrorInvalidPassword();
    }

    @Override
    public void showErrorInvalidEmail() {
        mRegisterView.showErrorInvalidEmail();
    }

    @Override
    public void showErrorInvalidPhoneNumber() {
        mRegisterView.showErrorInvalidPhone();
    }

    @Override
    public void showErrorPasswordLength() {
        mRegisterView.showErrorPasswordLength();
    }

    @Override
    public void showErrorPasswordConfirm() {
        mRegisterView.showErrorPasswordConfirm();
    }

    @Override
    public void createFormatWatcher() {
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots(PHONE_FORMAT);
        FormatWatcher formatWatcher = new MaskFormatWatcher(
                MaskImpl.createTerminated(slots)
        );
        mRegisterView.addPhoneNumberFormatting(formatWatcher);
    }

    @Override
    public void hideErrorsInvalid() {
        mRegisterView.hideErrorInvalidEmail();
        mRegisterView.hideErrorInvalidPassword();
        mRegisterView.hideErrorInvalidPhone();
        mRegisterView.hideErrorPasswordConfirm();
    }

    @Override
    public void showViewLoading() {
        mRegisterView.showLoading();
    }

    @Override
    public void hideViewLoading() {
        mRegisterView.hideLoading();
    }

    @Override
    public void uploadAvatar() {
        if (fileToUpload != null) {
            UseCase uploadFileUseCase = new UploadFileUseCase(new ContentDataRepository(mRegisterView.getContextActivity()),
                    ApiConstants.CONTENT_TYPE_IMAGE_JPEG, fileToUpload, CurrentUser.CURRENT_AVATAR);
            uploadFileUseCase.execute(new Subscriber<Response<Void>>() {
                @Override
                public void onCompleted() {
                    mRegisterView.showError(mRegisterView.getContextActivity().getString(R.string.avatar_uploaded));
                }

                @Override
                public void onError(Throwable e) {
                    String message = ErrorMessageFactory.
                            createMessageOnLogin(mRegisterView.getContextActivity(), e);
                    mRegisterView.showError(message);
                }

                @Override
                public void onNext(Response<Void> response) {

                }
            });
        }
    }

    @Override
    public void validateInformation(SignUpUserM informationOnCheck, String passwordConf) {
        this.hideErrorsInvalid();
        boolean isValidationSuccess = true;
        if (!mValidator.isValidEmail(informationOnCheck.getEmail())) {
            isValidationSuccess = false;
            this.showErrorInvalidEmail();
        }
        if (!mValidator.isValidPassword(informationOnCheck.getPassword())) {
            isValidationSuccess = false;
            this.showErrorInvalidPassword();
        }
        if (!mValidator.isValidPhoneNumber(informationOnCheck.getPhone())) {
            isValidationSuccess = false;
            this.showErrorInvalidPhoneNumber();
        }
        if (!mValidator.isPasswordLengthMatches(informationOnCheck.getPassword())) {
            isValidationSuccess = false;
            this.showErrorPasswordLength();
        }
        if (!mValidator.isPasswordMatch(informationOnCheck.getPassword(),
                passwordConf)) {
            isValidationSuccess = false;
            this.showErrorPasswordConfirm();
        }

        if (isValidationSuccess)
            this.registrationRequest(informationOnCheck);
    }

    @Override
    public void verifyAndLoadAvatar(Uri uri) {
        if (mValidator.isValidAvatarSize(mRegisterView.getContextActivity(), uri)) {
            fileToUpload = Converter.convertUriToFile(mRegisterView.getContextActivity(), uri);
            mRegisterView.loadAvatarToImageView(uri);
        } else {
            mRegisterView.showError(mRegisterView.getContextActivity().getString(R.string.too_large_picture_max_size_1mb));
        }
    }

    @Override
    public void registrationRequest(SignUpUserM userM) {
        userM.setPhone(mValidator.toApiPhoneFormat(userM.getPhone()));
        if (signUpRequestM == null)
            signUpRequestM = new SignUpRequestM();
        signUpRequestM.setUser(userM);

        signUpUseCase = new SignUpUseCase(new SessionDataRepository(), signUpRequestM);
        signUpUseCase.execute(new Subscriber<ResponseSignUpModel>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.toString());
                mRegisterView.showError(ErrorMessageFactory.
                        createMessageOnRegistration(mRegisterView.getContextActivity(), e));
            }

            @Override
            public void onNext(ResponseSignUpModel signUpModel) {
                Log.d(TAG, signUpModel.toString());
                uploadAvatar();
                onRegistrationSuccess(signUpModel);
            }
        });

    }


    @Override
    public void facebookLink(LoginResult loginResult) {
        Log.d("123", "Presenter Facebook request");
        mRegistrationModel.getFacebookLink(loginResult);

    }

    @Override
    public void onRegistrationSuccess(ResponseSignUpModel signUpModel) {
        mRegisterView.goToMainActivity(signUpModel);
        //mRegisterView.onRegistrationSuccess();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void destroy() {
        if (mRegisterView != null)
            mRegisterView = null;
    }

    @Override
    public void refreshLinkedInfInView(FacebookLinkInform linkInform) {
        Log.d("123", "callback " + linkInform.toString());
        mRegisterView.showError(mRegisterView.getContextActivity().getString(R.string.linked_with_facebook_user) + " "
                + linkInform.getFullName() + " Id " + linkInform.getUserId());
        CurrentUser.getInstance().setFacebookToken(linkInform.getToken());
        mRegisterView.refreshInfAfterFacebookLink(linkInform);
    }
}