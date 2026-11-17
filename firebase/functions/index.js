/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
const logger = require("firebase-functions/logger");
const crypto = require("crypto");
const { getMessaging } = require("firebase-admin/messaging");
const messaging = getMessaging();
const { onCall, HttpsError } = require("firebase-functions/v2/https");

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 1 });

// From Gemini, "Pick n random values from list javascript", 2026-11-13
function getUniqueRandom(arr, n) {
  // Clone the array to avoid mutating the original
  const shuffled = [...arr];

  // Bound n to the maximum length of the array
  const max = Math.min(n, shuffled.length);

  for (let i = 0; i < max; i++) {
   // Pick a random index from the remaining unswapped elements
   const randIndex = Math.floor(Math.random() * (shuffled.length - i)) + i;

   // Swap the elements
   [shuffled[i], shuffled[randIndex]] = [shuffled[randIndex], shuffled[i]];
  }

  return shuffled.slice(0, max);
}

// Lottery picker
exports.LotteryDraw = onSchedule("0 * * * *", async (event) => {
   // Take snapshot
   const lotteriesRef = db.collection("lotteries");
   const snapshot = await lotteriesRef.where("deadline", "<", Date.now()).get();

   // If we have a snapshot, loop over the entries
   if (!snapshot.empty) {
      snapshot.forEach(async (doc) => {
         // Pick the winners and update the doc
         let winners = getUniqueRandom(doc.get("entrantIDs"), doc.get("numWinners"));
         let docRef = doc(db, "lotteries", doc.get("id"));
         await updateDoc(docRef, {
            pendingWinnerIDs: winners
         });

         // Set a notification in the repository for each winner and loser
         doc.get("entrantIDs").forEach(async (entrantID) => {
            let notifId = crypto.randomUUID();
            let notifContent = winners.includes(entrantID) ? "You won a lottery" : "You lost a lottery";
            let notifData = {
               id: notifId,
               userId: entrantID,
               read: false,
               content: notifContent,
               navDestination: "" // TODO: Update this to take the user to the booking screen for the lottery if they won
            };
            // Add to firestore
            await db.collection("notifications").doc(notifId).set(notifData);
            // Push
            // Get user FID
            let userSnap = doc(db, "users", entrantID);
            let fid = userSnap.get("firebaseId");
            // Send if user allows
            if (userSnap.get("notifs")) {
               await messaging.send({
                  fid,
                  notification: {title: "Lottery Update", body: notifContent},
                  data: {},
               });
            }
         });
      });
   }
});

// Generic Push Notification Sender
exports.sendPushNotification = onCall(async (request) => {
   // Get data from request
   const userId = request.data.userId;
   const content = request.data.content
   // Get user FID
   let userSnap = doc(db, "users", userId);
   let fid = userSnap.get("firebaseId");
   // Send if user allows
   if (userSnap.get("notifs")) {
      await messaging.send({
         fid,
         notification: {title: "Lottery Update", body: content},
         data: {},
      });
   }
});
