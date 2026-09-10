<!-- SPDX-License-Identifier: Apache-2.0 -->
<!-- Copyright 2026 Aninda Sundar Howlader (GRU953) -->

# Getting started with Declutter

Written for a Motorola Edge 50 Fusion, and correct for any Android phone from version 9 onwards. No computer is needed, and nothing here is permanent.

Set aside twenty minutes for the first time. After that, restarting Shizuku takes about fifteen seconds.

---

## Before you start

Read this part. It is short and it is the part that matters.

Declutter removes preinstalled apps **for you only**. The copy your phone was built with stays on the phone's read-only storage, so Declutter can put anything back. What it cannot do is bring back an app's Play Store updates — a restored app is the version the phone shipped with — and it cannot undo anything from a phone that will not start. That second limit is why Declutter refuses, with no override, to touch anything the phone needs in order to work.

One optional step is worth ten minutes now and could save an afternoon later.

**Step 0 — show the phone a computer, once.** If you have a Windows, Mac or Linux computer:

1. On the phone, open **Settings**.
2. Tap **About phone**. On some Motorola builds it is **Settings › System › About phone**.
3. Tap **Build number** seven times. A message says you are now a developer.
4. Go back to **Settings › System › Developer options**.
5. Switch on **USB debugging**.
6. Connect the phone to the computer with a cable.
7. On the phone, a box asks **Allow debugging?**. Tick **Always allow from this computer**, then tap **Allow**.

You may never need this. But if a change ever left the phone's screen unusable, that authorised computer is what puts things back without wiping the phone — and Android will not accept a computer it has not already been shown.

---

## Part 1 — get Declutter onto the phone

1. On the phone, open the repository's **Actions** tab: `https://github.com/GRU953/Android/actions/workflows/android.yml`
2. Tap the newest run with a green tick.
3. Scroll down to **Artifacts**.
4. Tap **declutter-apk**. A `.zip` file downloads.
5. Open the **Files** app and find the downloaded `.zip`, usually in **Downloads**.
6. Tap the `.zip` to open it, then tap the `.apk` file inside.
7. Android says it cannot install from this source. Tap **Settings** in that message.
8. Switch on **Allow from this source**, then press Back.
9. Tap **Install**.

Declutter is now in your app drawer, with a bird on a dark blue tile.

---

## Part 2 — get Shizuku onto the phone

Android will not let an ordinary app remove a preinstalled one. Shizuku is a small free app that borrows the powers a computer has when a phone is plugged in for development, and lends them to apps you approve. No root, nothing permanent — the powers stop when the phone restarts.

1. On the phone, open `https://github.com/RikkaApps/Shizuku/releases/latest`
2. Scroll to **Assets** and tap the file whose name ends in **.apk**.
3. Open the downloaded file and tap **Install**.

Get it from there, not the Play Store. The Play copy lags behind and has been reported not to start on recent Android versions.

---

## Part 3 — switch Shizuku on

1. Open **Settings**.
2. Tap **About phone** — or **Settings › System › About phone**.
3. Tap **Build number** seven times, if you have not already.
4. Go to **Settings › System › Developer options**.
5. Make sure the phone is on **Wi-Fi**. This will not work on mobile data.
6. Switch on **Wireless debugging**.
7. A box appears asking to allow it. Tick **Always allow on this network**, then tap **Allow**. That tick is what lets Shizuku start itself again after a restart.
8. Tap the words **Wireless debugging** to open its screen.
9. Tap **Pair device with pairing code**. A six-digit code and a number appear.

**Now stop and read this one line: leave that box open.** Do not close it, do not press Back. Both the six-digit code and the number beside it change every time you open it, so closing the box makes the code you just typed wrong. This is the step almost everybody gets wrong the first time.

10. Swipe down from the top of the screen to open your notifications. Do not close the pairing box.
11. Tap Shizuku's notification. It asks for the pairing code.
12. Type the six digits you can see, and confirm.
13. Shizuku says it is paired. Now you can close the pairing box.
14. Open the **Shizuku** app and tap **Start**.

Shizuku now says it is running.

---

## Part 4 — let Declutter use it

1. Open **Declutter**.
2. It opens on the **Setup** tab. Tap **Check again**.
3. It says Shizuku is running and needs permission. Tap **Ask for permission**.
4. A box appears. Tap **Allow**.
5. The tab turns green and says **Ready**.

---

## Part 5 — remove something

1. Tap the **Apps** tab at the bottom.
2. You are looking at the preinstalled apps Declutter is willing to touch, most cautious first.
3. Tap any app to read what it does and what you lose without it. Nothing happens by tapping.
4. To remove things, tick their boxes, then tap **Remove** at the bottom right.
5. A box lists exactly what will go, and what each removal costs you. Read it.
6. Tap **Remove**.

Two shortcuts:

- **Tick all Safe** ticks everything on screen rated Safe. It only ticks — it never acts.
- **Quick Declutter**, the button that appears when nothing is ticked, offers every Safe-rated app at once. It still shows you the full list before it does anything.

A sensible first pass: remove five or six things you recognise and do not want, restart the phone, and use it for a day. Then come back.

---

## Part 6 — when you have finished for the day

Do this **last**, after all your removing, because switching wireless debugging off can stop Shizuku.

1. **Settings › System › Developer options › Wireless debugging › off.**

While wireless debugging is on, anyone on the same Wi-Fi can reach a port that grants shell-level access to the phone. Android is supposed to require pairing first, and in 2026 a flaw was found that let that check be bypassed. Use your own network, not a café's, and switch it off when you are done.

---

## Putting something back

1. Open **Declutter** and tap the **Changes** tab.
2. Every change is listed with the date and time.
3. Tap **Undo this batch** to reverse one session's worth, or **Put everything back** for all of it.
4. Single apps can also be put back from the **Changed** filter on the Apps tab.

Shizuku has to be running for any of this, exactly as it does for removing.

---

## After the phone restarts

Shizuku stops when the phone restarts. That is Android's doing, not a fault, and on a phone without root there is no way round it.

1. **Settings › System › Developer options › Wireless debugging › on.**
2. Open **Shizuku** and tap **Start**.
3. Open **Declutter**. It should say Ready.

You do **not** have to pair again — the pairing is remembered. If Shizuku will not start, switch Wireless debugging off and on again, then try once more.

---

## If something has gone wrong

**An app you wanted has vanished.** Declutter › Changes › Undo this batch.

**Shizuku keeps dying while you are using the phone.** Motorola's Battery Care software stops background apps even when told not to. Go to **Settings › Apps › Shizuku › Battery** and set it to **Unrestricted**.

**Declutter says a package cannot be turned off.** Motorola marks some packages as "must stay on" and Android refuses. That refusal is a good reason to leave it alone.

**The phone is behaving oddly and you are not sure what you removed.** Declutter › Changes › **Share the log**. It lists every change with its date, plus the phone's build number and patch level as they were before you started.

**The screen is unusable, or the phone will not get past the logo.** This is what Step 0 was for. On the computer you authorised, with ADB installed:

```
adb shell pm install-existing --user 0 com.example.thepackage
```

Then restart the phone. For something that was turned off rather than removed:

```
adb shell pm enable com.example.thepackage
```

The package names are in the log you can share from the Changes tab. If no computer was ever authorised, a factory reset restores every removed app — and erases everything else.

---

## Two things worth knowing

**A Motorola system update can quietly put removed apps back.** If they reappear after an update, that is why. Remove them again.

**Nothing here has been tested on a physical phone yet.** The code compiles, its safety tests pass, and its checks are clean — but the first person to run it is finding out something nobody has yet. Start small.

---

সহজ প্রযুক্তি। সবার জন্য। · Simple technology. For everyone.
