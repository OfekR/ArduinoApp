var firstBoardBarriers = ["B5", "B8", "B4", "B3"]
var firstBoardMines = ["M5","M1","M4"]
var firstBoardLootbox = ["L1","L2","L5"]
var secondBoardBarriers = ["B1", "B2", "B7"]
var secondBoardMines = ["M6","M2","M3"]
var secondBoardLootbox = ["L3","L4","L6"]

function setBit(num,index,isOn)
{
    //if (isChanged)
    let baseBitToSet = index*2;
    num |= 1 <<  (baseBitToSet + 1); //indicate change
    if(isOn)
    {
        num |= 1 <<  (baseBitToSet); //indicate turn on or turn off
    }
    return num;
}

exports.mineRequestNew = functions.https.onRequest((req, res) => {
    let mineArray = (Number(req.query.id) == 1) ? firstBoardMines : secondBoardMines;
    var dict ={};
    let promise1 = admin.database().ref("Mines").once('value');

    promise1.then(function(snapshot1) {
      var numMineToReturn = 0;
      for(let index =0; index < mineArray.length; ++index)
      {
        let mineName = mineArray[index];
        console.log(mineName)
        let curChild = snapshot1.child(mineName);
        if(curChild.child("board").val() == 1)
        {
          let isOn = (curChild.child("status").val() == 1);
          numMineToReturn = setBit(numMineToReturn, index, isOn)
          admin.database().ref("Mines").child(mineName).update({board: 0});
        }
      }
      console.log("finished mineRequest new");
      return res.status(200).send(numMineToReturn.toString());
    })
    .catch(error => {
      console.log(error);
      return res.status(500).send(error);
      })
  });

  exports.barrierRequestNew = functions.https.onRequest((req, res) => {
    let barrierArray = (Number(req.query.id) == 1) ? firstBoardBarriers : secondBoardBarriers;
    let promise1 = admin.database().ref("Barriers").once('value');

    promise1.then(function(snapshot1) {
      var numBarrierToReturn = 0;
      for(let index =0; index < barrierArray.length; ++index)
      {
        let barrierName = barrierArray[index];
        let curChild = snapshot1.child(barrierName);
        if(curChild.child("board").val() == 1)
        {
          let isOn = (curChild.child("status").val() == 1);
          numBarrierToReturn = setBit(numBarrierToReturn, index, isOn)
          admin.database().ref("Barriers").child(barrierName).update({board: 0});
        }
      }
      return res.status(200).send(numBarrierToReturn.toString());
    })
    .catch(error => {
      console.log(error);
      return res.status(500).send(error);
      })
  });

  exports.lootboxRequestNew = functions.https.onRequest((req, res) => {
    let lootoboxArray = (Number(req.query.id) == 1) ? firstBoardLootbox : secondBoardLootbox;
    let promise1 = admin.database().ref("Lootbox").once('value');

    promise1.then(function(snapshot1) {
      var numLootoboxToReturn = 0;
      for(let index =0; index < lootoboxArray.length; ++index)
      {
        let lootoboxName = lootoboxArray[index];
        let curChild = snapshot1.child(lootoboxName);
        if(curChild.child("board").val() == 1)
        {
          let isOn = (curChild.child("status").val() == 1);
          numLootoboxToReturn = setBit(numLootoboxToReturn, index, isOn)
          admin.database().ref("Lootbox").child(lootoboxName).update({board: 0});
        }
      }
      return res.status(200).send(numLootoboxToReturn.toString());
    })
    .catch(error => {
      console.log(error);
      return res.status(500).send(error);
      })
  });