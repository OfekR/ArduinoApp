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
    
    exports.resetLifeInGame = functions.https.onRequest((req, res) => {
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
                        var usersUpdate = {};
                        usersUpdate[`${player}`] = "20";
                        let updateData = tokenDoc.update(usersUpdate);
                        return res.status(200).send(old_val);
                    }
                })
    
             .catch(error => {
            console.log(error);
            return res.status(500).send(error);
                })
        });
        exports.resetvalidend = functions.https.onRequest((req, res) => {
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
                            let playervalid = "valid"+token;
                            var usersUpdate = {};
                            usersUpdate[`${playervalid}`] = "0";
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
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
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
    exports.setfirstGame = functions.https.onRequest((req, res) => {
        let keys = req.query.keys;
        let time = req.query.time;
        let mine = req.query.mine;
        let shots = req.query.shots;
        let type = req.query.type;
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
                        // Lets assume we get account balance from d
                        let updateData = tokenDoc.update({Duration:time, Keys:keys, Mines:mine, Shots:shots,Type:type});
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