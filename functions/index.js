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

    /*
    handle the hit of car and send data for reducing the life and add points to opp
    */
    exports.car1GatHit = functions.https.onRequest((req, res) => {
        let promise = admin.database().ref('Game').once('value');
        promise.then(function(snapshot) {
        var tmp =Number(snapshot.val().valid1);
        var life =Number(snapshot.val().life1);
        var points =Number(snapshot.val().points2);
        life= life -1;
        points = points + 10;
        tmp = tmp +1;
        admin.database().ref("/Game/").update({life1:life,points2:points,valid1:tmp})
        return res.status(200).send(`${tmp}`);
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      })
      });

      exports.car2GatHit = functions.https.onRequest((req, res) => {
        let promise = admin.database().ref('Game').once('value');
        promise.then(function(snapshot) {
        var tmp =Number(snapshot.val().valid2);
        var life =Number(snapshot.val().life2);
        var points =Number(snapshot.val().points1);
        life= life -1;
        points = points + 10;
        tmp = tmp +1;
        admin.database().ref("/Game/").update({life2:life,points1:points,valid2:tmp})
        return res.status(200).send(`${tmp}`);
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      })
      });

      

/**
 * handler for log and update the stats of player in end of the game
 */
   
exports.endOfGameSendder = functions.https.onRequest((req, res) => {
    let token = req.query.token;
    let flag = req.query.flag;
    let life = req.query.life
    let valid = req.query.valid;
    let points = req.query.points;
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
                    var pointplayer = "points"+token;
                    var lifeplayer = "life"+token;
                    var flagplayer = "flag"+token;
                    var validplayer = "valid"+token;
                    var usersUpdate = {};
                    usersUpdate[`${pointplayer}`] = points;
                    usersUpdate[`${lifeplayer}`] = life;
                    usersUpdate[`${flagplayer}`] = flag;
                    usersUpdate[`${validplayer}`] = valid;
                    let updateData = tokenDoc.update(usersUpdate);
                    return res.status(200).send();
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});

    /**
     * helper function for sending the end data to player stats 
     */

    exports.lstUpdate = functions.https.onRequest((req, res) => {
        let id = req.query.id;
        let time = parseInt(req.query.time);
        let play = parseInt(req.query.play);
        let lost = parseInt(req.query.lost);
        let pre = parseInt(req.query.pre);
        let won = parseInt(req.query.won);
        let mbomb = parseInt(req.query.mbomb);
        let mlaser = parseInt(req.query.mlaser);
        let tbomb = parseInt(req.query.tbomb);
        let thits = parseInt(req.query.thits);
        let points = parseInt(req.query.points);
        let shots = parseInt(req.query.shots);
        let flags = parseInt(req.query.flags);
        let numPlayedflags = parseInt(req.query.numPlayedflags);
        let numPlayedtime = parseInt(req.query.numPlayedtime);
        let numPlayedhighscore = parseInt(req.query.numPlayedhighscore);


                let tokenDoc = admin.firestore().collection('PlayerStats').doc(id);
                var promise = tokenDoc.get()
                promise.then(doc => {
                    //if token exists then send data otherwise error response
                    if (!doc.exists) {
                        console.log('Invalid token');
                        return res.status(200).send("Invalid token");
                    } else {
                        console.log('valid token');
                        real_data=doc.data();
                        let updateData = tokenDoc.update({bestTime:time, gamesLost:lost, gamesPlayed:play, gamesWon:won, hitsPercentage:pre, mostBombHits: mbomb
                        , mostLaserHits:mlaser, totalBombHits:tbomb, totalHits:thits, totalPoints:points, totalShots:shots,flags:flags,numPlayedflags:numPlayedflags,
                        numPlayedtime:numPlayedtime,numPlayedhighscore:numPlayedhighscore });
                        return res.status(200).send();
                    }
                })
    
             .catch(error => {
            console.log(error);
            return res.status(500).send(error);
                })
        });


        
      exports.rfidHandlerForCar1 = functions.https.onRequest((req, res) => {
        let card_id = req.query.cardId;
        let promise = admin.database().ref("Gates/card1/").once('value');
        promise.then(function(snapshot) {
        snapshot.forEach(function(childSnaphot){
            var data = childSnaphot.val().value;
            var idKey = childSnaphot.val().id;
            var gate = childSnaphot.key;
            console.log( childSnaphot.val());
            if(idKey === card_id){
                // no aprove to open the gate 
                if(data === '0'){
                return res.status(200).send('0');
                }
                else{
                    admin.database().ref("/Gates/card1/"+gate).update({value:'0'})
                    return res.status(200).send('1');
                }
            }
        })
        return 2;
        })
        .catch(error => {
            console.log(error);
            return res.status(500).send(error);
      })
      });



      exports.handleRfid = functions.https.onRequest((req, res) => {
        let id = Number(req.query.id);
        let param = req.query.param;
        //lotbox
        if(param === "lotbox"){
            var g_points,g_defuse,g_mine,g_keys,s_points,s_keys,s_defuse,s_mine
            var dict = {};
            console.log('lotbox');
            var gift = Math.floor((Math.random() * 4) + 1);
            let giftPromise = admin.database().ref('Game').once('value');
            giftPromise.then(function(giftSnapshot){
            // points + 100
            if( gift === 1){
                if(id === 1){
                    g_points =Number(giftSnapshot.val().points1);
                    s_points ="points1" 
                }
                else{
                    g_points =Number(giftSnapshot.val().points2);
                    s_points ="points2" 

                }
                g_points=g_points+100;
                dict[`${s_points}`] = g_points.toString();
            }
            //defuse
            else if(gift === 2){
                if(id === 1){
                    g_defuse =Number(giftSnapshot.val().defuse1); 
                    s_defuse ="defuse1" 
                }
                else{
                    g_defuse =Number(giftSnapshot.val().defuse2); 
                    s_defuse ="defuse2" 

                }
                g_defuse=g_defuse+1;
                dict[`${s_defuse}`] = g_defuse.toString();

            }
            //mine
            else if(gift === 3){
                if(id === 1){
                    g_mine =Number(giftSnapshot.val().mine1); 
                    s_mine = "mine1"
                }
                else{
                    g_mine =Number(giftSnapshot.val().mine2);
                    s_mine = "mine2" 

                }
                g_mine=g_mine+1;
                dict[`${s_mine}`] = g_mine.toString();

            }
            //keys
            else{
                if(id === 1){
                    g_keys =Number(giftSnapshot.val().keys1);
                    s_keys = "keys1" 
                }
                else{
                    g_keys =Number(giftSnapshot.val().keys2);
                    s_keys = "keys2"  

                }
                g_keys=g_keys+1;
                if(isNaN(g_keys)){
                    dict[`${s_keys}`] = "10"
                }
                else{
                dict[`${s_keys}`] = g_keys.toString()
                }
            }
            admin.database().ref("/Game/").update(dict);
            return res.status(200).send('lotbox');
        }).catch(error => {
            console.log(error);
            return res.status(500).send(error);
            });
        }
        //bomb
        else{
            // check if there defuse if there send back ok  and -- 1 defuse else send bomb and defuse car to 5 sec
            let minePromise = admin.database().ref('Mines').once('value');
            minePromise.then(function(mineSnapshot){
                var mine;
                console.log('Mines--part1');
                if(param === "mine1"){
                    mine =Number(mineSnapshot.val().mine1);
                }
                else if(param === "mine2"){
                    mine =Number(mineSnapshot.val().mine2);
                }
                else if(param === "mine3"){
                    mine =Number(mineSnapshot.val().mine3);
                }
                else{
                    mine =Number(mineSnapshot.val().mine4);
                }
                // we want to set mine if we have 
                let gameInside = admin.database().ref('Game').once('value');
                let returnVal = gameInside.then(function(gameMine){
                    var my_mine,nameField
                    var updateData = {};
                    var updateMine = {};
                    if(mine === 0){
                        console.log('Mines--part2');
                        if( id === 1){
                            my_mine =Number(gameMine.val().mine1);
                            nameField = "mine1"
                        }
                        else{
                            my_mine =Number(gameMine.val().mine2);
                            nameField = "mine2"
                        }
                        if(my_mine > 0){
                            console.log('Mines--part3');
                             //need to set new mine -1
                            my_mine = my_mine -1
                            updateData[`${nameField}`] = my_mine.toString();
                            updateMine[`${param}`] = "1";
                            admin.database().ref("/Game/").update(updateData);
                            admin.database().ref("/Mines/").update(updateMine);
                            return res.status(200).send('set');
                        }
                        else{
                            console.log('Mines--part4');
                            return res.status(200).send('no-bomb-to-set');  
                            }
                        }
                        
                        //the mine is set so we want too check if we can defuse it 
                        else{
                            console.log('Mines--part5');
                            var my_defuse,my_life,lifeFeild
                            if( id === 1){
                                my_defuse =Number(gameMine.val().defuse1);
                                my_life =Number(gameMine.val().life1);
                                nameField = "defuse1"
                                lifeFeild = "life1"

                            }
                            else{
                                my_defuse =Number(gameMine.val().defuse2);
                                my_life =Number(gameMine.val().life2);
                                vnameField = "defuse2"
                                lifeFeild = "life2"
                            }
                            //can defuse
                            if(my_defuse > 0){
                                console.log('Mines--part6');
                                //need to set new defuse -1
                                my_defuse = my_defuse -1
                                my_mine = my_mine -1
                                updateMine[`${param}`] = "0";
                                updateData[`${nameField}`] = my_defuse.toString();
                                admin.database().ref("/Game/").update(updateData);
                                admin.database().ref("/Mines/").update(updateMine);
                                return res.status(200).send('defuse');
                            }
                            else{
                                // the bomb strack you
                                console.log('Mines--part7');
                                my_life = my_life-5
                                my_mine = my_mine -1
                                updateMine[`${param}`] = "0";
                                updateData[`${lifeFeild}`] = my_life.toString();
                                admin.database().ref("/Game/").update(updateData); 
                                admin.database().ref("/Mines/").update(updateMine);
                                return res.status(200).send('injured');  
                            }
                        }
                    
                    }).catch(error => {
                    console.log(error);
                    return res.status(500).send(error);
                    });
                  //  throw error("bad place");
            return res.status(200).send("end-need-to-fix");  
            }).catch(error => {
            console.log(error);
            return res.status(500).send(error);
            });
        }  
      });
       