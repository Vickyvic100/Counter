# Hysam Foam Counter

Offline physical stock-counting and auditing application for foam/mattress products.

## How to Download the APK

Every time a change is pushed to this repository, a new APK is automatically built using GitHub Actions.

1. Click on the **Actions** tab at the top of this GitHub repository.
2. Click on the most recent workflow run (usually named "Build Android APK").
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click on **HysamFoamCounter-Debug** to download the ZIP file containing the APK.
5. Extract the ZIP and install the `.apk` file on your Android device.

## Features

- **100% Offline**: No internet required for scanning or counting.
- **QR Code Scanning**: Scans SKU and Code from product JSON.
- **Duplicate Protection**: Prevents counting the same SKU twice in a batch.
- **Local Persistence**: All data stored in an on-device SQLite database.
- **Export**: Generate CSV reports and share via Android share sheet.
