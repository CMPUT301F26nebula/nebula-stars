/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const { setGlobalOptions } = require("firebase-functions");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
const logger = require("firebase-functions/logger");
const crypto = require("crypto");
const { getMessaging } = require("firebase-admin/messaging");
const messaging = getMessaging();
const { onCall, HttpsError } = require("firebase-functions/v2/https");

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
  const lotteriesRef = db.collection("lotteries");
  const snapshot = await lotteriesRef.where("deadline", "<", Date.now()).get();

  if (snapshot.empty) {
    return;
  }

  for (const lotteryDoc of snapshot.docs) {
    const entrantIDs = lotteryDoc.get("entrantIDs") || [];
    const numWinners = lotteryDoc.get("numWinners") || 0;
    const winners = getUniqueRandom(entrantIDs, numWinners);

    // Update the lottery document directly via its DocumentReference
    await lotteryDoc.ref.update({
      pendingWinnerIDs: winners,
    });

    // Create notifications and send push messages for all entrants
    await Promise.all(
      entrantIDs.map(async (entrantID) => {
        const notifId = crypto.randomUUID();
        const notifContent = winners.includes(entrantID)
          ? "You won a lottery"
          : "You lost a lottery";
        const notifData = {
          id: notifId,
          userId: entrantID,
          read: false,
          content: notifContent,
          navDestination: "", // TODO: Update this to take the user to the booking screen for the lottery if they won
        };

        // Add to Firestore
        await db.collection("notifications").doc(notifId).set(notifData);

        // Fetch user snapshot and send push notification if enabled
        const userSnap = await db.collection("users").doc(entrantID).get();
        const fid = userSnap.get("firebaseId");

        if (userSnap.exists && userSnap.get("notifs") && fid) {
          await messaging.send({
            token: fid,
            notification: { title: "Lottery Update", body: notifContent },
            data: {},
          });
        }
      })
    );
  }
});

// Generic Push Notification Sender
exports.sendPushNotification = onCall(async (request) => {
  const userId = request.data.userId;
  const content = request.data.content;

  // Fetch user snapshot
  const userSnap = await db.collection("users").doc(userId).get();
  const fid = userSnap.get("firebaseId");

  // Send if user exists, allows notifications, and has a valid token
  if (userSnap.exists && userSnap.get("notifs") && fid) {
    await messaging.send({
      token: fid,
      notification: { title: "Lottery Update", body: content },
      data: {},
    });
  }
});