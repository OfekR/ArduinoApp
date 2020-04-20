const functions = require('firebase-functions');
const admin = require('firebase-admin');
const _ = require('lodash');

admin.initializeApp();
const db = admin.firestore();

exports.addJoin = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('Appending').doc('Append1');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    old_val = real_data["waitTojoin"];
                    let updateData = tokenDoc.update({waitTojoin: token});
                    var accountBal = '$200';
                    return res.status(200).send(old_val);
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});




exports.setGameReady = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('Appending').doc('Append1');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    old_val = real_data["gameReady"];
                    let updateData = tokenDoc.update({gameReady: token});
                    var accountBal = '$200';
                    return res.status(200).send(old_val);
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});

exports.chnageLifeInGame = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('Game').doc('firstgame');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    let player = "LifePlayer"+token;
                    old_val = real_data[player];
                    new_val = parseInt(old_val) - parseInt("1")
                    var usersUpdate = {};
					usersUpdate[`${player}`] = new_val.toString();
                    let updateData = tokenDoc.update(usersUpdate);
                    return res.status(200).send(old_val);
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});

exports.endOfGameSendder = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('Game').doc('endgame');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    var player = "pointsPlayer"+token;
                    old_val = real_data[player];
                    var usersUpdate = {};
					usersUpdate[`${player}`] = token;
                    new_val = parseInt(old_val) - parseInt("1")
                    let updateData = tokenDoc.update(usersUpdate);
                    return res.status(200).send(old_val);
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});


exports.setPlayerId1 = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('GameSettings').doc('doucment1');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    let updateData = tokenDoc.update({playerId1: token});
                    return res.status(200).send();
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});


exports.setPlayerId2 = functions.https.onRequest((req, res) => {
    let token = req.query.token;
            let tokenDoc = admin.firestore().collection('GameSettings').doc('doucment1');
            var promise = tokenDoc.get()
            promise.then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    return res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    real_data=doc.data();
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    let updateData = tokenDoc.update({playerId2: token});
                    return res.status(200).send();
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});

exports.updateUserData = functions.firestore
  .document(`Logs/{userId}`)
  .onUpdate(async (snap, context) => {
    let user_id = snap.after.get("ID");
    let time  = snap.after.get('TIME-LEFT-IN-SEC');
    let defuse  = snap.after.get('DEFUSE-END-OF-GAME');
    let keys  = snap.after.get('KEYS-END-OF-GAME');
    let life  = snap.after.get('LIFE-END-OF-GAME');
    let mines  = snap.after.get('MINES-END-OF-GAME');
    let num_bomb  = snap.after.get('NUM-BOMB-END-OF-GAME');
    let num_hits  = snap.after.get('NUM-HITS-END-OF-GAME');
    let num_shots  = snap.after.get('NUM-SHOTS-END-OF-GAME');
    let points  = snap.after.get('POINTS-END-OF-GAME');
    let status  = snap.after.get('STATUS-END-OF-GAME');
    let type  = snap.after.get('TYPE-END-OF-GAME');
    var lost =0;
    var win = 0;
    if(status.localeCompare("LOSE")){
       lost=1;
    }            
    if(status.localeCompare("WIN")){
        win=1;
    }


    /*
    let docRef =  admin.firestore().collection('PlayerStats').doc('uGCWlsGywrVfO52EGHf27hZICn03');
    let promise = await docRef.get();
    promise.then(doc =>{
        var _bestTime  = doc.get('bestTime');
        var _gamesLost  = doc.get('gamesLost');
        var _gamesPlayed  = doc.get('gamesPlayed');
        var _gamesWon  = doc.get('gamesWon');
        var _hitsPercentage  = doc.get('hitsPercentage');
        var _mostBombHits  = doc.get('mostBombHits');
        var _mostLaserHits  = doc.get('mostLaserHits');
        var _totalBombHits  = doc.get('totalBombHits');
        var _totalHits  = doc.get('totalHits');
        var _totalPoints  = doc.get('totalPoints');
        var _totalShots  = doc.get('totalShots');    
        if(_bestTime < time){
            _bestTime = time;
        }
        if(status.localeCompare("LOSE")){
            _gamesLost = _gamesLost +1;
        }
        if(status.localeCompare("WIN")){
            _gamesWon = _gamesWon+1;
        }
        _gamesPlayed = _gamesPlayed+1;
        if( _mostBombHits < num_bomb){
            _mostBombHits = num_bomb;
        }
        if( _mostLaserHits < num_hits){
            _mostLaserHits = num_hits;
        }
        _totalBombHits = _totalBombHits +num_bomb;
        _totalHits = _totalHits +num_hits;
        _totalPoints = __totalPoints + points;
        _totalShots = _totalShots + num_shots;
        if(_totalShots !== 0){
            _hitsPercentage = _totalHits/_totalShots;	
        }
        */
       await admin
      .firestore()
      .doc(`PlayerStats/${user_id}`)
      .update({
            bestTime: time,
            gamesLost: lost,
            gamesPlayed: 1,
            gamesWon: win,
            hitsPercentage: 5,
            mostBombHits: num_bomb,
            mostLaserHits: num_hits,
            totalBombHits: num_bomb,
            totalHits: num_hits,
            totalPoints: points,
            totalShots: num_shots
      });

    return 0;
  });

/*
exports.listnerUserData = functions.firestore
    .document(`PlayerStats/{userId}`)
    .onUpdate(async (snap, context) => {
        let userId = snap.id;
        console.log(userId);

        // before
        var _bestTime  = snap.before.get('bestTime');
        var _gamesLost  = snap.before.get('gamesLost');
        var _gamesPlayed  = snap.before.get('gamesPlayed');
        var _gamesWon  = snap.before.get('gamesWon');
        var _hitsPercentage  = snap.before.get('hitsPercentage');
        var _mostBombHits  = snap.before.get('mostBombHits');
        var _mostLaserHits  = snap.before.get('mostLaserHits');
        var _totalBombHits  = snap.before.get('totalBombHits');
        var _totalHits  = snap.before.get('totalHits');
        var _totalPoints  = snap.before.get('totalPoints');
        var _totalShots  = snap.before.get('totalShots');
            //after
        var bestTime  = snap.after.get('bestTime');
        var gamesLost  = snap.after.get('gamesLost');
        var gamesPlayed  = snap.after.get('gamesPlayed');
        var gamesWon  = snap.after.get('gamesWon');
        var hitsPercentage  = snap.after.get('hitsPercentage');
        var mostBombHits  = snap.after.get('mostBombHits');
        var mostLaserHits  = snap.after.get('mostLaserHits');
        var totalBombHits  = snap.after.get('totalBombHits');
        var totalHits  = snap.after.get('totalHits');
        var totalPoints  = snap.after.get('totalPoints');
        var totalShots  = snap.after.get('totalShots');    
        if(_bestTime < bestTime){
            _bestTime = bestTime;
        }
        if( _mostBombHits < mostBombHits){
            _mostBombHits = mostBombHits;
        }
        if( _mostLaserHits < mostLaserHits){
            _mostLaserHits = mostLaserHits;
        }
        _gamesPlayed = _gamesPlayed + gamesPlayed;
        _gamesLost = _gamesLost +gamesLost;
        _gamesWon = _gamesWon +gamesWon;
        _totalBombHits = _totalBombHits + mostBombHits;
        _totalHits = _totalHits +totalHits;
        _totalPoints = _totalPoints + totalPoints;
        _totalShots = _totalShots + totalShots;
        if(_totalShots !== 0){
            _hitsPercentage = _totalHits/_totalShots;	
        }
        await admin
        .firestore()
        .doc(`PlayerStats/${userId}`)
        .set({
              bestTime: _bestTime,
              gamesLost: _gamesLost,
              gamesPlayed: _gamesPlayed,
              gamesWon: _gamesWon,
              hitsPercentage: _hitsPercentage,
              mostBombHits: _mostBombHits,
              mostLaserHits: _mostLaserHits,
              totalBombHits: _totalBombHits,
              totalHits: _totalHits,
              totalPoints: _totalPoints,
              totalShots: _totalShots
        });
  
      return 0;
        });
*/