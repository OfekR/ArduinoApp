var rfidMapping = {
	30994: "S1",
	14605: "S2",
	30986: "F3",
	35306: "F4",
	39171: "L1",
	51538: "L2",
	14740: "L3",
	35281: "L4",
    55799: "L5",
	47416: "L6",
	31216: "B1",
	10703: "B2",
	31096: "B3",
	6449: "B4",
	14098: "B5",
	63933: "B6",
	42821: "B7",
	6501: "B8",
	22891: "M1",
	31081: "M2",
	10586: "M3",
	2326: "M4",
	39244: "M5",
	59792: "M6",
	30995: "ES1",
	14606: "ES2",
	30987: "EF3",
	35307: "EF4",
	39172: "EL1",
	51539: "EL2",
	14741: "EL3",
	35282: "EL4",
	47417: "EL5",
	55800: "EL6",
	31217: "EB1",
	10704: "EB2",
	31097: "EB3",
	6450: "EB4",
	14099: "EB5",
	63934: "EB6",
	42822: "EB7",
	6502: "EB8",
	22892: "EM1",
	31082: "EM2",
	10587: "EM3",
	2327: "EM4",
	39245: "EM5",
	59793: "EM6",
};

var PlayerMapping = {
    1 : "Player1",
    2 : "Player2"
};

var rfidTypeMapping = {
    "S" : 0,
    "F" : 1,
    "L" : 2,
    "B" : 3,
    "M" : 4,
    "E" : 5
};

/*******************************  Barriers *******************************/
	function checkRegularBarrier(playerId, uid, res) {
        let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]

        let promise = admin.database().ref(fireBaseLiveGameInfoPath).once('value');
		promise.then(function(snapshot) {
		//Check if player have keys
		var key;
        key =Number(snapshot.val().keys);

        var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Barrier"

		if(key != 0) {
            // player have keys, let app know 
            admin.database().ref(fireBasePath).update({OpenBarrier: rfidMapping[uid]});
            return res.status(200).send("EnabledBarrier");        
        }
        else {
            //No keys 
            admin.database().ref(fireBasePath).update({OpenBarrier: "99"});
            return res.status(200).send("NoKeys");        
        }
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      })
        ;
    }
    
	function checkSpecialBarrier(playerId, uid, res) {
        let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]

        let promise = admin.database().ref(fireBaseLiveGameInfoPath).once('value');
		promise.then(function(snapshot) {
		//Check if player have keys
        var key, barrierName;
        key =Number(snapshot.val().specialKeys);
        //player1
        if(playerId === 1) {
            barrierName = "B7";
        }
        //player 2
        else {
            barrierName = "B8";
        }
		if(key != 0) {
            // player have keys open barrier
            var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Barrier"
            admin.database().ref(fireBasePath).update({OpenBarrierSpecial: 1});
            var fireBaseOpenBarrierPath = "/Barriers/" + barrierName;
            admin.database().ref(fireBaseOpenBarrierPath).update({status: 1});
            return res.status(200).send("OpenedSpecialbarrier");        
        }
        else {
            //No keys 
            var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Barrier"
            admin.database().ref(fireBasePath).update({OpenBarrierSpecial: 99});
            return res.status(200).send("NoSpecialKeys");        
        }
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      });
        
        
	}

	function handleBarrierStart(playerId, uid, res) {
        let fireBaseBarrierPath = "Barriers/" + rfidMapping[uid] + "/";

        let promise = admin.database().ref(fireBaseBarrierPath).once('value');
		promise.then(function(snapshot) {
		//Check if gate already open
        var gateStatus = Number(snapshot.val().status);
        if(gateStatus == 0)
        {
            //check if it's special or regular barrier
            var barrierName = rfidMapping[uid];
            var barrierNumber = barrierName.slice(-1);
            if(barrierNumber > 6) {
              //Special barrier, check if this user special barrier
              if((playerId==1 && barrierNumber == 7) ||  playerId == 2 && barrierNumber == 8) {
                  return checkSpecialBarrier(playerId, uid, res);
              }
              else 
              {
                // otherwise, special barrier of wrong player => nothing to do 
                var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Barrier"
                admin.database().ref(fireBasePath).update({OpenBarrierSpecial: 9});
                return res.status(200).send("WrongSpecialBarrier");        
              }

            }
            else {
              return checkRegularBarrier(playerId, uid, res)
            }
        }
        //else - gate already open, nothing to do
        return res.status(200).send("GateAlreadyOpen");        
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      });
        	
    }
    
    function handleBarrierEnd(playerId, uid, res) {
        var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Barrier"
        admin.database().ref(fireBasePath).update({OpenBarrier: "0"}); 
        admin.database().ref(fireBasePath).update({OpenBarrierSpecial: 0}); 
        return res.status(200).send("BarrierEnd");        
	}

/*******************************  Start and Finish *******************************/
    function handleStartingPointStart(playerId, uid, res) {
        if((rfidMapping[uid] == "S1" && playerId == 1) || (rfidMapping[uid] == "S2" && playerId == 2))
        {
            var fireBasePath = "/RfidReading/" + PlayerMapping[playerId];
            admin.database().ref(fireBasePath).update({Start: 1}); 
            return res.status(200).send("StartingPointStart");    
        }

        // other user starting point
        return res.status(200).send("WrongStartingPointStart");    
    }

    function handleStartingPointEnd(playerId, uid, res) {
        var fireBasePath = "/RfidReading/" + PlayerMapping[playerId];
        admin.database().ref(fireBasePath).update({Start: 0}); 
        return res.status(200).send("StartingPointEnd");        
    }
    
    function handleFinishingPointStart(playerId, uid, res) {
        if(rfidMapping[uid] == "F3" && playerId == 1) {
            let fireBaseLiveGameInfoPath = "/LiveGameinfo/"
            admin.database().ref(fireBaseLiveGameInfoPath).update({gameEnd: 31}); 
            return res.status(200).send("FinishingPointStart1");        

        }
        else if(rfidMapping[uid] == "F4" && playerId == 2) {
            let fireBaseLiveGameInfoPath = "/LiveGameinfo/"
            admin.database().ref(fireBaseLiveGameInfoPath).update({gameEnd: 32}); 
            return res.status(200).send("FinishingPointStart2");        
        }

        // other user finishing point
        return res.status(200).send("WrongFinishingPointStart");          
    }

    function handleFinishingPointEnd(playerId, uid, res) {
        //Note - when finishing game should be over, no need to reset this variable
        //var fireBasePath = "/RfidReading/" + PlayerMapping[playerId];
        //admin.database().ref(fireBasePath).update({Finish: 0}); 
        return res.status(200).send("FinishingPointEnd");        
    }
    

/*******************************  Mines *******************************/

function handleMineArmed(playerId, uid, res) {
    let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]

    let promise = admin.database().ref(fireBaseLiveGameInfoPath).once('value');
    promise.then(function(snapshot) {
        //check if user can disable mine (have defuser)
        var defuseMines, defuseName;
        defuseMines = Number(snapshot.val().defuser)

        var fireBaseUserMinePath = "/RfidReading/" + PlayerMapping[playerId] + "/Mine"
        var fireBaseMinesPath = "Mines/" + rfidMapping[uid] + "/";

        if (defuseMines == 0) {
            //explode - update app and disable mine
            admin.database().ref(fireBaseUserMinePath).update({Explode: 1});
            admin.database().ref(fireBaseMinesPath).update({status: 0});
            var otherPlayerId = (playerId == 1) ? 2 : 1;
            admin.database().ref("/EnemyInjured/" + PlayerMapping[otherPlayerId]).update({enemyExplode: 1});


            return res.status(200).send("MineExploded");      
        }
        else {
            // got mine defuser - update app and disable mine and reduce mine defuser
            admin.database().ref(fireBaseUserMinePath).update({Disarm: 1});
            admin.database().ref(fireBaseMinesPath).update({status: 0});

            admin.database().ref(fireBaseLiveGameInfoPath).update({"defuser": (--defuseMines)});
            return res.status(200).send("MineDefused");      

        }
    })
    .catch(error => {
        console.log(error);
        return res.status(500).send(error);
  });
}

function handleMineUnarmed(playerId, uid, res) {
    let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]

    let promise = admin.database().ref(fireBaseLiveGameInfoPath).once('value');
    promise.then(function(snapshot) {
    //Check if player have mines
    var mine;
    mine =Number(snapshot.val().mines);

    var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Mine"
    if(mine != 0) {
        // player have mine to be able to place it
        admin.database().ref(fireBasePath).update({PlaceMine: rfidMapping[uid]});
        return res.status(200).send("EnabledMine");        
    }
    else {
        //No mine 
        admin.database().ref(fireBasePath).update({PlaceMine: "99"});
        return res.status(200).send("NoMines");        
    }
    })
    .catch(error => {
        console.log(error);
        return res.status(500).send(error);
  });
}

function handleMineStart(playerId, uid, res) {
    let fireBaseMinesPath = "Mines/" + rfidMapping[uid] + "/";

    let promise = admin.database().ref(fireBaseMinesPath).once('value');
    promise.then(function(snapshot) {
    //Check if gate already open
    var mineStatus = Number(snapshot.val().status);
    if(mineStatus == 0)
    {
        //Unarmed mine
        return handleMineUnarmed(playerId, uid, res);
    }
    else if(mineStatus == 1)
    {
        return handleMineArmed(playerId, uid, res);
    } 
    else //mineStatus == 2
    {
        //blinking mine (was set few moments ago), nothing to do
        return res.status(200).send("MineBlinking");        
    }

    })
    .catch(error => {
        console.log(error);
        return res.status(500).send(error);
  });}

function handleMineEnd(playerId, uid, res) {
    var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/Mine"
    admin.database().ref(fireBasePath).update({PlaceMine: "0"}); 
    return res.status(200).send("MineEnd");  }

/*******************************  LootBox *******************************/
function handleRegularLootbox(playerId, uid, res) {

    /*Gift list:
    1 - Key
    2 - Ammo
    3 - Mine
    4 - DisarmMine
    5 - Points
    6 - Additional life
    */
    var gift = Math.floor((Math.random() * 6) + 1);
    var giftName;
    //TODO decide about how much to add from every type
    var additionalKey = 1;
    var additionalAmmo = 10;
    var additionalMine = 1;
    var additionalDefuse = 1;
    var additionalPoints = 40;
    var additionalLife = 5;


    let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]

    let giftPromise = admin.database().ref(fireBaseLiveGameInfoPath).once('value');
    giftPromise.then(function(giftSnapshot){
        switch(gift) {
            case(1): //Key
            giftName = "Key";
            keyNumber =Number(giftSnapshot.val().keys);
            admin.database().ref(fireBaseLiveGameInfoPath).update({keys: (keyNumber + additionalKey)});
            break;    

            case(2): //Ammo
            giftName = "Ammo";
            ammoNumber =Number(giftSnapshot.val().ammo);
            admin.database().ref(fireBaseLiveGameInfoPath).update({ammo: (ammoNumber + additionalAmmo)});
            break;    

            case(3): //Mine
            giftName = "Mine";
            mineNumber =Number(giftSnapshot.val().mines);
            admin.database().ref(fireBaseLiveGameInfoPath).update({mines: (mineNumber + additionalMine)});
            break;   
            
            case(4): //Defuse
            giftName = "Defuse";
            defuseNumber =Number(giftSnapshot.val().defuser);
            admin.database().ref(fireBaseLiveGameInfoPath).update({defuser: (defuseNumber + additionalDefuse)});
            break;    

            case(5): //Points
            giftName = "Points";
            pointsNumber =Number(giftSnapshot.val().points);
            admin.database().ref(fireBaseLiveGameInfoPath).update({points: (pointsNumber + additionalPoints)});

            break;   
            
            case(6): //Life
            giftName = "Life";
            lifeNumber =Number(giftSnapshot.val().life);
            admin.database().ref(fireBaseLiveGameInfoPath).update({life: (lifeNumber + additionalLife)});
            break;    

            default:
                giftName = "ErrorGift"
                break;
        }

    console.log("LootBoxPicked"+giftName);
    //Set lootbox unavaliable and update app about the gift got (app responsible to ReActive lootbox)
    let fireBaseLootboxPath = "Lootbox/" + rfidMapping[uid] + "/";
    admin.database().ref(fireBaseLootboxPath).update({status: 1});      
    var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/LootBox"
    var giftWithUid = gift+rfidMapping[uid];
    admin.database().ref(fireBasePath).update({PickedLootbox: giftWithUid});
    return res.status(200).send(("LootBoxPicked"+giftWithUid));

}).catch(error => {
    console.log(error);
    return res.status(500).send(error);
    });
}

function handleLootboxStart(playerId, uid, res) {
    let fireBaseLootboxPath = "Lootbox/" + rfidMapping[uid] + "/";
    //check if it's special or regular barrier
    var LootBoxName = rfidMapping[uid];
    var LootBoxNumber = LootBoxName.slice(-1);
    var isSpecialLootbox = (LootBoxNumber > 4)

    let promise = admin.database().ref(fireBaseLootboxPath).once('value');
    promise.then(function(snapshot) {
    //Check if lootbox is avaliable
    var lootboxStatus = Number(snapshot.val().status); 
    var fireBasePath = "/RfidReading/" + PlayerMapping[playerId] + "/LootBox"

    if (lootboxStatus == 1) {
        //lootbox unavaliable 
        if(isSpecialLootbox) {
            admin.database().ref(fireBasePath).update({PickedSpecialLootbox: 99});
        }
        else {
            admin.database().ref(fireBasePath).update({PickedLootbox: "99"});
        }
        return res.status(200).send("unavailableLootbox");        
    }
    else {
        if(isSpecialLootbox) {
            if((LootBoxNumber == 5 && playerId == 2) || (LootBoxNumber == 6 && playerId == 1)) {
                // this lootbox can only be picked by oppenent 
                admin.database().ref(fireBasePath).update({PickedSpecialLootbox: 9});
                return res.status(200).send("opponentLootbox");        
            }
            //legal special lootbox - pick it up:
            //Set lootbox unavaliable and update app about the gift got 
            let fireBaseLootboxPath = "Lootbox/" + rfidMapping[uid] + "/";
            admin.database().ref(fireBaseLootboxPath).update({status: 1});      
            admin.database().ref(fireBasePath).update({PickedSpecialLootbox: 1});

            let fireBaseLiveGameInfoPath = "/LiveGameinfo/" + PlayerMapping[playerId]
            admin.database().ref(fireBaseLiveGameInfoPath).update({"specialKeys": (1)});
            return res.status(200).send("PickedSpecialLootbox");        

        }
        else {
            return handleRegularLootbox(playerId,uid,res);
        }
    }
    }).catch(error => {
        console.log(error);
        return res.status(500).send(error);
        });
}

function handleLootboxEnd(playerId, uid, res) {
    return res.status(200).send("LootboxEnd");        
}


/*******************************  RFIDCloudFunction *******************************/

      /*
      this function do all the handle of the rfid chip 
      */
      exports.handleRfidRead = functions.https.onRequest((req, res) => {

        let playerId = Number(req.query.id);
        let uid = Number(req.query.param);

        console.log("Got Uid: " + String(uid) + " From player " + String(playerId));

        var type = rfidMapping[uid].slice(0,1);
        //identify which rfid type was read
      
        switch(rfidTypeMapping[type]) {
          case 0:
            console.log("Start");
            return handleStartingPointStart(playerId,uid, res);
      
          case 1:
            console.log("Finish");
            return handleFinishingPointStart(playerId,uid, res);
          
          case 2:
            console.log("Lootbox");
            return handleLootboxStart(playerId,uid, res);
      
          case 3:
            console.log("Barrier");
            return handleBarrierStart(playerId,uid, res);
      
          case 4:
            console.log("Mine");
            return handleMineStart(playerId,uid, res);                  
      
          case 5:
            var type2 = rfidMapping[uid].slice(1,2);
      
            switch(rfidTypeMapping[type2]) {
                case 0:
                    console.log("EndStart");
                    return handleStartingPointEnd(playerId,uid, res);
      
                case 1:
                    //Note - this function does nothing 
                    console.log("EndFinish");
                    return handleFinishingPointEnd(playerId,uid, res);
          
                 case 2:
                    //nothing to do
                    console.log("EndLootbox");
                    return handleLootboxEnd(playerId,uid, res);
      
                case 3:
                    console.log("EndBarrier");
                    return handleBarrierEnd(playerId,uid, res);
      
                case 4:
                    console.log("EndMine");
                    return handleMineEnd(playerId,uid, res);                  
      
           
      
        }
      }
      }      
      );