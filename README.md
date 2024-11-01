# Virtual Wiscard

**Virtual Wiscard** is a mobile application that virtualizes the UW-Madison Wiscard, allowing users to scan into dorms, gyms, or dining halls directly with their phones. This app provides the same functionalities as the physical Wiscard, supporting NFC scanning, balance checking, and transaction viewing.

---

## Project Members

- **Eric Erdman**
    - 📧 ererdman2@wisc.edu
    - 🔗 GitHub: [Eric-Erdman](https://github.com/Eric-Erdman)

- **Jake Garneau**
    - 📧 jgarneau@wisc.edu
    - 🔗 GitHub: [Jake-Garneau](https://github.com/Jake-Garneau)

- **Zhihai Hu**
    - 📧 hu434@wisc.edu
    - 🔗 GitHub: [FrdHuy](https://github.com/FrdHuy)

- **Kristen Menge**
    - 📧 kmenge@wisc.edu
    - 🔗 GitHub: [kmenge](https://github.com/kmenge)

---

## Project Background

Many students often forget to bring their physical Wiscards, which restricts access to dorms, dining halls, gyms, and other campus facilities. This app provides a solution by offering a virtual Wiscard that allows easy access and payments through scanning. The app also supports balance checking, transaction viewing, and customizable settings for a seamless experience.

---

## Features

- **Virtual Wiscard**: Displays a virtual Wiscard on the phone, allowing NFC scanning for access and payments.
- **User Authentication**: Integrates with UW-Madison's SSO system for secure login.
- **Balance Checking**: Allows users to view their current Wiscard balance and top-up funds.
- **Transaction History**: Displays a history of recent transactions, including details of each purchase.
- **Customizable Settings**: Users can adjust session duration, login persistence, and other preferences.

---

## Wireframe

![Wireframe](https://www.figma.com/proto/8n6X1RA58DnQgNCy06OolT/Untitled?node-id=4-2&t=Zg5lG3EA9QZZ6pAV-1&starting-point-node-id=4%3A2)

- View the full Figma prototype above, which includes screens for login, home, NFC scanning, balance checking, and settings.

---

## Project Structure

### Key Modules

1. **Login Module**
    - Handles UW-Madison SSO login.
    - Redirects to the main screen upon successful login.

2. **Home Screen Module**
    - Displays the virtual Wiscard for NFC scanning access to dorms, dining halls, etc.

3. **NFC Module**
    - Activates NFC functionality for Wiscard scanning.
    - Provides "scan successful" or "scan failed" feedback.

4. **Balance Module**
    - Shows the user’s current balance and allows top-up.

5. **Transaction Module**
    - Displays recent transactions with details.

6. **Settings Module**
    - Provides customization options for session duration, login persistence, and notifications.

7. **Backend Server Module**
    - Manages server-side operations like authentication and data storage.

8. **Database Module**
    - Stores user login information, balance, and transaction history.

---

## Development Tools

- **Android Studio**: Development platform.
- **Figma**: For prototype design and user feedback.


