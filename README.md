FlowerStoreApp

===

\## 📝 Hướng dẫn Setup PayOS cho Android Emulator (dùng backend Render.com)



\### ❗ Vấn đề thường gặp



Khi test tính năng thanh toán bằng PayOS trên Android Emulator, bạn có thể gặp lỗi:



```

Unable to resolve host "your-backend.onrender.com": No address associated with hostname

```



Lý do là do Android Emulator không thể resolve DNS của các domain như Render.com khi chưa được cấu hình đúng DNS.



---



\### ✅ Cách khắc phục: Cấu hình DNS Google (8.8.8.8) khi chạy emulator



\#### 🧰 Bước 1: Xác định tên AVD (Android Virtual Device)



Mở \*\*Terminal hoặc PowerShell\*\*, chạy lệnh:



```bash

"C:\\Users\\LENOVO\\AppData\\Local\\Android\\Sdk\\emulator\\emulator.exe" -list-avds

```



Lệnh này sẽ hiển thị danh sách các emulator có sẵn, ví dụ:



```

Pixel\_6\_API\_34

Nexus\_5X\_API\_30

```



---



\#### 🛠️ Bước 2: Tạo file `start\_emulator.bat`



Tạo file mới tên `start\_emulator.bat` trong thư mục gốc project hoặc bất kỳ vị trí nào bạn dễ chạy. Dán đoạn sau vào file:



```bat

@echo off

REM Đổi tên emulator bên dưới thành tên bạn lấy được từ bước 1

start "" "C:\\Users\\LENOVO\\AppData\\Local\\Android\\Sdk\\emulator\\emulator.exe" -avd Pixel\_6\_API\_34 -dns-server 8.8.8.8

```



> 📌 Lưu ý:

> - Đường dẫn có thể thay đổi tùy máy, bạn có thể kiểm tra SDK path trong Android Studio:  

> `File > Settings > Appearance \& Behavior > System Settings > Android SDK`



---



\#### ▶️ Bước 3: Chạy emulator bằng file `.bat`



\- Đảm bảo emulator đang tắt.

\- Chạy file `start\_emulator.bat` bằng cách double-click hoặc qua terminal.

\- Đợi emulator khởi động.

\- Giờ app Android trong emulator sẽ truy cập được domain `.onrender.com`.



---



\### ✅ Kiểm tra nhanh bằng ADB



Sau khi emulator chạy, bạn có thể kiểm tra:



```bash

\# Kiểm tra thiết bị đã kết nối

"C:\\Users\\LENOVO\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe" devices



\# Mở shell và thử ping domain

"C:\\Users\\LENOVO\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe" shell

ping prm392-finalproject.onrender.com

```



---



\### 🎯 Kết quả mong đợi



\- Android app mở link thanh toán từ PayOS thành công.

\- Không còn lỗi `UnknownHostException` trong logcat.



---



\### 📌 Ghi chú thêm



\- Giải pháp này \*\*chỉ áp dụng khi test với Android Emulator\*\*.

\- Nếu bạn test bằng \*\*thiết bị thật\*\*, lỗi này sẽ không xảy ra.

\- Nếu backend \*\*không deploy bằng Render.com\*\*, bạn có thể không cần thay đổi DNS.

