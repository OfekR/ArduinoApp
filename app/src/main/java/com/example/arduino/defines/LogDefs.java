package com.example.arduino.defines;

public final class LogDefs {

    //private c'tor to simulate singleton
    private LogDefs() {

    }
    //Tags
    public static final String tagLogin = "RegisterLogin";
    public static final String tagSuccess = "T_Success";
    public static final String tagDebug = "T_Debug";



    // login and register
    public static final String emailLoginSucMsg = "signInWithEmail:success";
    public static final String emailLoginFailMsg = "signInWithEmail:failure";
    public static final String emailRegisterSucMsg = "createUserWithEmail:success";
    public static final String emailRegisterFailMsg = "createUserWithEmail:failure";
    public static final String emailInvalidlMsg = "email value invalid";
    public static final String passwordInvalidlMsg = "password value invalid";




}
