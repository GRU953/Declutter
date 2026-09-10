<picture>
  <source media="(prefers-color-scheme: dark)" srcset=".github/readme-header-dark.png">
  <img src=".github/readme-header-light.png" alt="Declutter by GRU953 — remove preinstalled apps without root." width="100%">
</picture>

# Declutter

**Declutter by GRU953 is an Android app that removes the preinstalled apps your phone came with — the ones you never chose and cannot uninstall — without root, and without a computer after the first setup.**

সহজ প্রযুক্তি। সবার জন্য। · Simple technology. For everyone.

[![Licence Apache-2.0](https://img.shields.io/badge/licence-Apache--2.0-1A1753?style=flat-square&labelColor=0B0E14)](LICENSE)
[![Version](https://img.shields.io/badge/version-0.1.0-1A1753?style=flat-square&labelColor=0B0E14)](../../releases)

---

## What it does

Android will not let an ordinary app remove a preinstalled one. Declutter borrows the same temporary powers a computer has when a phone is plugged in for development, through a free helper app called [Shizuku](https://github.com/RikkaApps/Shizuku/releases/latest). No root. No unlocked bootloader. Nothing permanent — the powers stop when the phone restarts.

Removing an app takes it away for you only. The copy the phone was built with stays on the read-only system partition, so Declutter can put it back.

Every package carries a plain-English name, a description of what it does, and what you lose if it goes — shown before the action, not filed in a help page. Ratings are **Safe**, **Advanced**, **Expert** and **Never remove**, plus **Unchecked** for anything the catalogue has not looked at. Unchecked is never shown as safe.

**365 researched packages**, reviewed on 10 September 2026: 195 Safe, 83 Advanced, 45 Expert, 42 Never remove. 119 of them are Motorola or Lenovo, because the phone this was built for is a Motorola Edge 50 Fusion. The ratings came from that handset's own firmware, from Android's source, and from the reports of people who have broken their phones doing this.

## What it refuses to do

This is the part worth reading twice.

Android protects almost nothing here. Reading AOSP's own `PackageManagerService`, the only packages the system itself refuses to remove are the device owner, the provisioning package, active device admins, in-use shared libraries, and anything under a user restriction. `com.android.systemui`, `com.android.settings` and even `android` are not on that list. A shell-privileged caller can remove them and the phone will not start again.

So Declutter carries its own refusal list: **117 packages by name and 21 name patterns**, in Kotlin source, never in the catalogue file and never fetched from the internet — a bad catalogue update must not be able to unlock a bricking package. There is no override. No long-press, no developer mode, no "I know what I am doing" toggle.

That tier is deliberately stricter than the tools written for people who can reflash a phone. Google Play services, the Play Store and the WebView provider are rated "expert, at your own risk" elsewhere, and removing them does not stop a phone booting. They are refused here because this app is for someone who cannot recover a phone that has stopped working properly, and because losing the Play Store takes away the means to undo everything else. That is a judgement, not a grading anybody else has agreed to.

On top of that, four things are worked out fresh on every scan, because a fixed list cannot know them:

- the launcher you are using now, and whether another one is installed;
- your keyboard, and how many you have enabled — losing the only one means you cannot type at all, not even a PIN;
- the app that makes your calls and the app that receives your texts, including one-time passcodes;
- any app running an accessibility service you have switched on.

A `CatalogueSafetyTest` fails the build if any package the catalogue rates "never remove" is not also refused in code. It also checks the reverse: that ordinary bloatware is still removable, because a guard broad enough to block everything is just a broken app.

## What it does not do

- It does not delete anything from storage. "Remove" means removed for you.
- It does not touch a work profile or a second user. Only the phone's main profile.
- It does not send anything anywhere. No account, no analytics, no internet permission.
- It does not keep an app's updates. A restored app is the version the phone shipped with.
- It cannot undo a change from a phone that will not start. That is why it refuses to touch anything load-bearing — and why the setup screen asks you to authorise a computer for USB debugging once, before you begin, as the escape route you will probably never need. From such a computer, `adb shell pm install-existing --user 0 <package>` puts any one package back with no data loss and no factory reset.
- It does not run below Android 9. The reflection it relies on has no exemption path there.

## Install

There is no Play Store listing. Download the APK a GitHub build produced:

1. Open the [Actions tab](../../actions/workflows/android.yml) on the phone and tap the newest green run.
2. Scroll to **Artifacts** and tap **declutter-apk** to download it.
3. Open the downloaded `.zip`, then open the `.apk` inside it.
4. Android asks once whether to allow installing from this source. Allow it.

Then follow the Setup tab inside the app. It walks through Shizuku step by step.

If you would rather read the whole thing first, [`docs/GETTING-STARTED.md`](docs/GETTING-STARTED.md) is the same route written out click by click, including what to do if something goes wrong.

If Android refuses to install an update over an older copy, remove the old copy first. Builds are signed with Android's fallback debug key unless a release key is configured, and two different keys cannot replace each other.

### Building it yourself

```bash
git clone https://github.com/gru953/android.git
cd android
./gradlew assembleRelease
```

Needs JDK 17 or newer and an Android SDK with platform 37. The APK lands in `app/build/outputs/apk/release/`. It is 2.8 MB.

### Signing releases properly

Set four repository secrets and the build uses them instead of the fallback key: `DECLUTTER_KEYSTORE_PATH`, `DECLUTTER_KEYSTORE_PASSWORD`, `DECLUTTER_KEY_ALIAS`, `DECLUTTER_KEY_PASSWORD`. Nothing needs changing in the code.

## How it works

The reading half is ordinary public API plus `QUERY_ALL_PACKAGES`, so the whole list, every rating and every explanation work before Shizuku is set up at all. You can see exactly what the app would do before granting it any power.

The writing half goes through Shizuku to the same framework calls the platform's own `pm` command makes: `IPackageInstaller.uninstall` with `DELETE_SYSTEM_APP` and `DELETE_KEEP_DATA` for removal, `installExistingPackageAsUser` to put it back, `setApplicationEnabledSetting` to turn something off. Written from the AOSP AIDL definitions, so the only third-party code linked in is Shizuku's own API and a hidden-API bypass.

A removal is not reported as successful because the call did not throw. The real answer arrives asynchronously, and is then checked against what the phone actually reports. A batch is written to disk before the first change is attempted, and stops at the first failure.

## Limits I know about

- The catalogue was built for one handset on its 2026 firmware. Package sets change with firmware, network and region. An entry is evidence, not a promise.
- 18 of the 365 entries are marked low confidence. The app says so on the entry.
- A Motorola system update can quietly put removed apps back.
- None of this has been tested on a physical device yet. It compiles, its safety tests pass, lint is clean — but the first person to run it is finding out something I have not.

## Contributing

This is a personal project. Issues are welcome, especially "this package is on my phone and Declutter has never heard of it" — that is the most useful thing anybody can report. I usually reply within a week.

## Licence

**Code:** Apache-2.0. Use it, change it, sell it, no permission needed. See [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE).

**Not licensed:** the name **GRU953**, the Soaring Bird mark, the app-icon tile and the GRU953 wordmark. They identify the studio, so they stay with it. You may say your work uses GRU953's system; you may not present your work as GRU953's.

*Not legal advice.*

---

## ডিক্লাটার

**ডিক্লাটার একটি অ্যান্ড্রয়েড অ্যাপ। ফোনের সাথে আগে থেকে দেওয়া যে অ্যাপগুলো আপনি চাননি এবং সরাতেও পারেন না, সেগুলো এটি সরিয়ে দেয় — রুট ছাড়া, এবং প্রথমবার সেটআপের পর কম্পিউটার ছাড়াই।**

সহজ প্রযুক্তি। সবার জন্য।

### এটি কী করে

অ্যান্ড্রয়েড সাধারণ কোনো অ্যাপকে প্রি-ইনস্টল করা অ্যাপ সরাতে দেয় না। ডিক্লাটার [Shizuku](https://github.com/RikkaApps/Shizuku/releases/latest) নামের একটি ফ্রি সহায়ক অ্যাপের মাধ্যমে সেই ক্ষমতা ধার করে — ঠিক যে ক্ষমতা কম্পিউটারে ফোন লাগালে পাওয়া যায়। রুট লাগে না, বুটলোডার আনলক করতে হয় না, স্থায়ী কোনো পরিবর্তনও হয় না। ফোন রিস্টার্ট হলেই সেই ক্ষমতা চলে যায়।

কোনো অ্যাপ সরালে তা শুধু আপনার জন্য সরে যায়। ফোনের সাথে আসা মূল কপি সিস্টেম পার্টিশনে অক্ষত থাকে, তাই ডিক্লাটার সেটি ফিরিয়ে আনতে পারে।

প্রতিটি প্যাকেজের সাথে সহজ ভাষায় লেখা থাকে — এটি কী কাজ করে, আর সরালে আপনি কী হারাবেন। লেখাটি কাজ করার আগেই দেখানো হয়, কোনো হেল্প পাতায় লুকিয়ে রাখা হয় না। রেটিং চারটি: **নিরাপদ**, **অ্যাডভান্সড**, **এক্সপার্ট**, **কখনো সরাবেন না**। আর যেগুলো যাচাই করা হয়নি, সেগুলো **অযাচাইকৃত** হিসেবে দেখানো হয় — কখনো নিরাপদ বলে নয়।

১০ সেপ্টেম্বর ২০২৬ পর্যন্ত যাচাই করা **৩৬৫টি প্যাকেজ** এতে আছে। এর ১১৯টি মোটোরোলা বা লেনোভোর, কারণ যে ফোনের জন্য এটি বানানো সেটি একটি Motorola Edge 50 Fusion।

### যা এটি করতে দেয় না

অ্যান্ড্রয়েড এখানে প্রায় কোনো সুরক্ষা দেয় না। `com.android.systemui`, `com.android.settings`, এমনকি `android` — এগুলোও সিস্টেমের নিজের সুরক্ষা তালিকায় নেই। শেল-অনুমতি পাওয়া যেকোনো প্রোগ্রাম এগুলো সরিয়ে দিতে পারে, আর তারপর ফোন আর চালু হবে না।

তাই ডিক্লাটার নিজের একটি অস্বীকৃতি তালিকা রাখে: নাম ধরে **১১৭টি প্যাকেজ** আর **২১টি নামের প্যাটার্ন**। এই তালিকা কোডের ভেতরে থাকে — ক্যাটালগ ফাইলে নয়, ইন্টারনেট থেকেও আসে না। এটি অগ্রাহ্য করার কোনো উপায় রাখা হয়নি, ইচ্ছে করেই।

এর বাইরে প্রতিবার স্ক্যানের সময় চারটি জিনিস নতুন করে দেখা হয়: আপনি এখন কোন লঞ্চার ব্যবহার করছেন; আপনার কিবোর্ড এবং কতগুলো কিবোর্ড চালু আছে (একমাত্র কিবোর্ড হারালে পিনও লিখতে পারবেন না); যে অ্যাপ কল করে আর যে অ্যাপ এসএমএস নেয়; এবং আপনি চালু রেখেছেন এমন কোনো অ্যাক্সেসিবিলিটি সার্ভিস।

### যা এটি করে না

- স্টোরেজ থেকে কিছু মুছে ফেলে না। "সরানো" মানে শুধু আপনার জন্য সরানো।
- ওয়ার্ক প্রোফাইল বা দ্বিতীয় ইউজারে হাত দেয় না। শুধু ফোনের মূল প্রোফাইল।
- কোথাও কোনো তথ্য পাঠায় না। অ্যাকাউন্ট নেই, অ্যানালিটিক্স নেই, ইন্টারনেট অনুমতিও নেই।
- অ্যাপের আপডেট ফিরিয়ে দিতে পারে না। ফিরিয়ে আনা অ্যাপটি ফোনের সাথে আসা সংস্করণ।
- ফোন চালু না হলে কোনো পরিবর্তন ফেরানো যায় না। এ কারণেই এটি জরুরি কোনো কিছুতে হাত দেয় না।
- অ্যান্ড্রয়েড ৯-এর নিচে চলে না।

### সীমা

ক্যাটালগটি একটি নির্দিষ্ট ফোনের ২০২৬ সালের ফার্মওয়্যার দেখে বানানো। ফার্মওয়্যার, অপারেটর আর দেশ অনুযায়ী প্যাকেজের তালিকা বদলায়, তাই কোনো এন্ট্রি প্রমাণ — নিশ্চয়তা নয়। ৩৬৫টির মধ্যে ১৮টি এন্ট্রি কম নিশ্চিত, অ্যাপ সেটি বলে দেয়। মোটোরোলার সিস্টেম আপডেট সরানো অ্যাপ চুপচাপ ফিরিয়ে আনতে পারে। আর এখনো কোনো আসল ফোনে এটি চালানো হয়নি — কোড কম্পাইল হয়, নিরাপত্তা টেস্ট পাস করে, লিন্ট পরিষ্কার, কিন্তু প্রথম যিনি চালাবেন তিনি এমন কিছু জানবেন যা আমি এখনো জানি না।

### লাইসেন্স

**কোড:** Apache-2.0। ব্যবহার করুন, বদলান, বিক্রি করুন — অনুমতি লাগবে না।

**লাইসেন্স করা হয়নি:** **GRU953** নাম, Soaring Bird চিহ্ন, অ্যাপ-আইকন টাইল এবং GRU953 ওয়ার্ডমার্ক। এগুলো স্টুডিওর পরিচয়, তাই স্টুডিওর সাথেই থাকে।

*এটি আইনি পরামর্শ নয়।*
