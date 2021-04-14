package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //google login
    private static final int RC_SIGN_IN = 1;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    TextView txt;
    Button google_singout_btn;

    //gmail
    String gmail;
    TextView username_gmail;
    Button gmail_singin_btn;
    EditText gmail_edit;

    //fb
    CallbackManager mCallbackManager;
    LoginButton loginButton;
    FirebaseAuth mAuth_fb;
    Button fb_Logout_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.txt_username);
        google_singout_btn = findViewById(R.id.google_singout_btn);

        username_gmail = findViewById(R.id.txt_username_gmail);
        gmail_singin_btn = findViewById(R.id.gmail_login_btn);
        gmail_edit = findViewById(R.id.gmail_edit);
        fb_Logout_btn = findViewById(R.id.fb_Logout_btn);

        fb_Logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();  //firebase登出
                LoginManager.getInstance().logOut();//fb登出
                txt.setText("");
                loginButton.setVisibility(View.VISIBLE);
                fb_Logout_btn.setVisibility(View.GONE);
            }
        });

//        gmail_singin_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                gmail = gmail_edit.getText().toString();
//                gmailLogin();
//            }
//        });

        google_singout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登出
                mGoogleSignInClient.signOut();
                txt.setText("");
            }
        });

//        googleLogin();



        facebookLogin();

    }

    private void facebookLogin() {
        mAuth_fb = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);

        if(mAuth_fb.getCurrentUser()!=null){
            loginButton.setVisibility(View.GONE);
            fb_Logout_btn.setVisibility(View.VISIBLE);
        }

        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("***", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("***", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("***", "facebook:onError", error);
                // ...
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("***", "handleFacebookAccessToken:" + token);

        try {
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth_fb.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("***", "signInWithCredential:success");
                                FirebaseUser user = mAuth_fb.getCurrentUser();
                                updateUI(user);
                                loginButton.setVisibility(View.GONE);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("***", "signInWithCredential:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });
        } catch (Exception e) {
            Log.v("***", "error : " + e);
        }

    }

    private void gmailLogin() {
        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl("https://jsonplaceholder.typicode.com/posts")
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName("com.example.firebase",
                                true, /* installIfNotAvailable */
                                null   /* minimumVersion */)
                        .build();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendSignInLinkToEmail(gmail, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("***", "Email sent.");
                    } else {
                        Log.d("***", "Email no sent." + task.getException());
                    }
                });


    }

    private void googleLogin() {
        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(view -> {
            //signin
            try {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } catch (Exception e) {
                Log.v("***", "error :" + e);

            }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //resultCode == -1 確定有登入成功
        Log.v("***", "res :" + resultCode);

        // Result returned from launching the Intent from GoogleSignInclient.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN && resultCode == -1) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            GoogleSignInAccount account = null;
//            try {
//                //Google Sign In was successful, authenticate with Firebase
//                account = task.getResult(ApiException.class);
//            } catch (ApiException e) {
//                Log.v("***", "error :" + e);
//            }
//            firebaseAuthWithGoogle(account);
//        }
    }

    /*用戶成功GoogleSignInAccount ，從GoogleSignInAccount對象獲取ID令牌
    將GoogleSignInAccount交換為Firebase憑據，然後使用Firebase憑據向Firebase進行身份驗證*/
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                updateUI(null);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.v("***", "error :" + e);
        }

    }

    @Override
    protected void onStart() {
        // // Check if user is signed in (non-null) and update UI accordingly.
        super.onStart();

        //google
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account !=null){
//            txt.setText(account.getDisplayName());
//        }else {
//            Log.v("***","account :" + account);
//        }

        //fb
        FirebaseUser currentUser = mAuth_fb.getCurrentUser();
        if(currentUser != null){
            Log.v("***","currentUser :" + currentUser.getDisplayName());
            txt.setText(currentUser.getDisplayName());
        }else {
            Log.v("***","currentUser :" + currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            txt.setText(user.getDisplayName());
        }
    }
}