# MyanmarConnect (မြန်မာကွန်နက်)

> **Android-First Social Media & Community Platform for Myanmar Migrant Workers Living in Thailand**

---

## 🇲🇲 Overview (အကျဉ်းချုပ်)

MyanmarConnect is an Android social application designed specifically for the 2+ million Myanmar migrant workers living and working across Thailand (Bangkok, Samut Sakhon/Mahachai, Chiang Mai, Chonburi, Mae Sot, Surat Thani, Ranong).

The app eliminates language, isolation, and legal barriers by providing an authentic, supportive community in **Burmese (မြန်မာဘာသာ)**.

---

## ✨ Core Features Implemented

1. **User Authentication & Providers (Firebase Authentication)**:
   - **Google Sign-In**: Integrated using Android Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) and `googleid`.
   - **Email & Password Provider**: Integrated via `FirebaseAuth.createUserWithEmailAndPassword` and `signInWithEmailAndPassword` with registration and validation.
   - **Phone Number SMS OTP Provider**: Integrated via `PhoneAuthProvider.verifyPhoneNumber` and `PhoneAuthProvider.getCredential` supporting Thai (+66) and Myanmar (+95) phone formats.
   - **Session State Management**: Monitored in real-time with `FirebaseAuth.AuthStateListener` and exposed via Kotlin `StateFlow`.
   - Profile management: Display Name, Thai location selection (Bangkok, Mahachai, Chiang Mai, etc.), workplace occupation, bio, and profile photo.
   - Account deletion option ("အကောင့် အပြီးတိုင် ဖျက်သိမ်းရန်") ensuring complete data erasure.

2. **Home Feed (ပင်မစာမျက်နှာ)**:
   - Real-time community timeline with posts, timestamps, author Thai locations, and group tags.
   - Like button with like counters and instant state reflection.
   - Comments bottom sheet: View discussion thread and post new comments.
   - Share button: Triggers native Android Share sheet.
   - Post creation modal with multiline input, group selector, and image attachments.

3. **Pre-loaded Community Groups (အုပ်စုများ)**:
   - **အလုပ်ပါမစ်နှင့် ဗီဇာအထောက်အထား** (*Work Permits & Visa Documentation*): MOU, Pink Card, CI renewals, and 90-day reports.
   - **စက်ရုံနှင့် အလုပ်အကိုင် အခွင့်အလမ်းများ** (*Factory & Job Opportunities*): Vacancies, daily wages, and overtime notices.
   - **မဟာချိုင်နှင့် ဘန်ကောက် အသိုက်အဝန်း** (*Mahachai & Bangkok Community Hub*): Room rentals, peer support, and living tips.
   - **ဥပဒေအကူအညီနှင့် အလုပ်သမားအခွင့်အရေး** (*Legal Aid & Labor Rights Hotline*): Labor protection, compensation, and NGO contacts.
   - Group detail feeds and join/leave membership toggles.

4. **1-on-1 Direct Chat (Chat - မက်ဆေ့ခ်ျများ)**:
   - Inbox conversations list with unread markers and latest message previews.
   - 1-to-1 message stream with real-time bubble delivery, sender/recipient alignment, and conversation history.
   - Message privacy: Strictly protected so only conversation participants can access their dialogue.

5. **In-App Notifications (အသိပေးချက်)**:
   - Real-time alerts for post likes, comments, incoming direct messages, and group activities.
   - Unread badges and one-tap "Mark all as read" action.
   - Direct navigation from notification to post or chat conversation.

6. **Image Upload with Validation (ဓာတ်ပုံတင်ခြင်း)**:
   - Zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
   - Client-side file size validation (< 5MB limit).
   - Loading indicator during processing, preview card, remove photo action, and localized Burmese error alerts.

7. **Safety & Moderation (လုံခြုံရေးနှင့် ထိန်းကျောင်းမှု)**:
   - Report Post dialog with categorized reasons (Scam, illegal broker, hate speech, labor exploitation).
   - Block User feature: Instantly filters out blocked users' posts and direct messages.
   - Community Guidelines modal ("လူမှုစည်းကမ်းများ").
   - Privacy Policy modal ("ကိုယ်ရေးအချက်အလက် လုံခြုံမှုမူဝါဒ").

8. **Google AdMob Integration Ready (ကြော်ငြာနေရာ)**:
   - Material 3 Banner placeholder component ready for AdMob banner unit IDs.
   - Does not simulate fake clicks or fake revenue counters.

---

## 🛠️ Technical Architecture & Zero-Dollar Budget

- **Language & Framework**: Kotlin + Jetpack Compose with Material Design 3.
- **Architecture**: Single Activity architecture (`MainActivity`) with Model-View-Repository pattern and reactive `StateFlow` streams.
- **Zero-Dollar Initial Strategy**:
  - Starts instantly with high-performance local reactive caching and sample data.
  - Image handling utilizes zero-permission Photo Picker and local URIs, avoiding initial cloud storage billing tiers.
  - Cloud Firestore schema (`firebase-blueprint.json`) and security rules (`firestore.rules`) are fully prepared for seamless free-tier Spark deployment.

---

## 🗄️ Database Schema & Security Rules

### Collections in `firebase-blueprint.json`
- `/users/{uid}`: Worker profile data.
- `/posts/{postId}`: Feed posts and metadata.
- `/posts/{postId}/comments/{commentId}`: Discussion comments.
- `/groups/{groupId}`: Community discussion groups.
- `/chats/{conversationId}/messages/{messageId}`: Private 1-to-1 encrypted chat stream.
- `/notifications/{notificationId}`: Targeted worker notifications.
- `/reports/{reportId}`: Moderation report logs.

### Security Enforcements (`firestore.rules`)
- Strict authorization: `request.auth != null`.
- Ownership check: Users can only modify their own profile, posts, and comments.
- Privacy isolation: Chat messages can only be read or written by the sender or recipient (`senderId == request.auth.uid || recipientId == request.auth.uid`).
- Read-only moderation reports for standard users.

---

## 🚀 Running and Testing the App

1. The project compiles with modern Gradle Kotlin DSL (`gradle assembleDebug`).
2. Run automated tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
3. In AI Studio, the streaming Android emulator will automatically refresh and display the app on screen.
