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
                    let updateData = tokenDoc.update({LifePlayer1:new_val.toString()});
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
                    new_val = parseInt(old_val) - parseInt("1")
                    let updateData = tokenDoc.update({pointsPlayer1:num});
                    return res.status(200).send(old_val);
                }
            })

         .catch(error => {
		console.log(error);
		return res.status(500).send(error);
			})
	});



