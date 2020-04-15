package com.example.arduino.defines;

public final class LogDefs {

    //private c'tor to simulate singleton
    private LogDefs() {

    }
    //Tags
    public static final String tagLogin = "RegisterLogin";
    public static final String tagMenu = "MenuActivity";



    // login and register
    public static final String emailLoginSucMsg = "signInWithEmail:success";
    public static final String emailLoginFailMsg = "signInWithEmail:failure";
    public static final String emailRegisterSucMsg = "createUserWithEmail:success";
    public static final String emailRegisterFailMsg = "createUserWithEmail:failure";
    public static final String emailInvalidlMsg = "email value invalid";
    public static final String passwordInvalidlMsg = "password value invalid";


    //Main Menu
    public static final String signoutMsg = "signing out user";
    public static final String setupFirebaseListener = "setupFirebaseListener: setting up the auth state listener";
    public static final String userSignedInIs = "onAuthStateChanged: user signed in is: ";








}
