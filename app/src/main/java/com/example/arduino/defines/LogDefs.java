package com.example.arduino.defines;

public final class LogDefs {

    //private c'tor to simulate singleton
    private LogDefs() {

    }
    //Tags
    public static final String tagLogin = "RegisterLogin";
    public static final String tagMenu = "MenuActivity";
    public static final String tagBT = "BtRelated";
    public static final String tagGameScreen = "GameScreen";
    public static final String tagRFID = "Rfid";
    public static final String tagLiveGameInfo = "LiveGameInfo";
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

    //BT
    public static final String btEnabledSuccessfully = "BT was enabled successfully";
    public static final String btEnabledFailed = "BT failed to enable";
    public static final String btWasDisabled = "BT was disabled, asking user to turn it on";
    public static final String btConnectedSuccessfully = "BT was connected successfully";
    public static final String btConnectedFailed = "BT failed to connect";
    public static final String btDeviceNotSupported = "Device doesn't support BT";

    public static final String btDiscoveryDevicesStartedSuc = "Discovering other bluetooth devices";
    public static final String btDiscoveryDevicesStartedFail = "Fail to start discovering other bluetooth devices";

    public static final String btQueryPairedDeviceSuc = "Paired device was found";
    public static final String btQueryPairedDeviceFail = "Failed to find paired device";

    public static final String btFailedToDiscoverArduino = "Failed to discover arduino";

    //game screen
    public static final String gameStarting = "Game starting";
    public static final String gameStartingListenerFailed = "Game starting listener fail to read or empty db";
    public static final String rfidStartingListenerFailed = "Rfid starting listener fail to read or empty db";


    //RFID
    //Barrier
    public static final String OpenBarrierEnabled = "Got OpenBarrier enable of: ";
    public static final String OpenBarrierDisabled = "Got OpenBarrier disabled of: ";
    public static final String OpenBarrierNoKeys = "Got OpenBarrier with no keys";
    public static final String OpenBarrierSpecial = "Opened special barrier";
    public static final String OpenBarrierSpecialNoKeys = "Got OpenBarrierSpecial with no keys";
    public static final String OpenBarrierSpecialOppents = "Got OpenBarrierSpecial of oppenet";
    //Mine
    public static final String PlaceMineEnabled = "Got PlaceMine enable of: ";
    public static final String PlaceMineDisabled = "Got PlaceMine disabled of: ";
    public static final String PlaceMineNoMines = "Got PlaceMine with no mines";
    public static final String DisarmMines = "Mine was disarmed";
    public static final String ExplodeMine = "Mine Exploded";
    //LootBox
    public static final String LootBoxGot = "Got From LootBox: ";
    public static final String LootBoxUnavailable = "LootBox is unavailable ";
    public static final String LootBoxSpecialGot = "Got Special Key";
    public static final String LootBoxSpecialUnavailable = "Got LootBoxSpecial is unavailable ";
    public static final String  LootBoxSpecialOppents = "Got LootBoxSpecial of oppenet";

}
