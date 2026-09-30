const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "gen-lang-client-0715957218";
const DATABASE_ID = "ai-studio-android-legacyfa-1bed7fe0-5592-417e-b6a2-4b59bf905f16";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read tasks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("tasks").get());
});

test("Unauthenticated user: cannot create user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc("test").set({
      userId: "test",
      displayName: "Test User",
      email: "test@example.com",
      role: "Adult",
      familyId: "family_1",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "Family Administrator",
      familyId: "williams_family",
      avatarEmoji: "👩",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      role: "Adult Member",
      familyId: "williams_family",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read another user's task", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("tasks").doc("bob_task").set({
      id: "bob_task",
      userId: BOB_UID,
      familyId: "williams_family",
      title: "Bob chore",
      assignedTo: "Bob",
      dueDate: "2026-09-30",
      isCompleted: false,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("tasks").doc("bob_task").get());
});

test("Authenticated user: can read own task", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("tasks").doc("alice_task").set({
      id: "alice_task",
      userId: ALICE_UID,
      familyId: "williams_family",
      title: "Alice chore",
      assignedTo: "Alice",
      dueDate: "2026-09-30",
      isCompleted: false,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("tasks").doc("alice_task").get());
});
