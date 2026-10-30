# ساخت APK اپ Gold Scalp

این یک پوسته‌ی کوچک اندروید است که صفحه‌ی اپ موبایل (سرور `gold_mobile`) را تمام‌صفحه نشان می‌دهد. دکمه‌های BUY/SELL/CLOSE و ورودی لات همان‌جا هستند.
اولین بار آدرس سرور را می‌پرسد (مثلاً `http://192.168.1.10:8000` یا آدرس Tailscale). برای تغییر آدرس: دکمه‌ی **Volume Up** را نگه دارید.

## راه ۱: ساخت APK با GitHub (بدون نصب چیزی روی کامپیوتر)

1. در github.com یک مخزن **خصوصی (private)** جدید بسازید (مثلاً `goldscalp-apk`).
2. محتوای همین پوشه را در آن آپلود کنید (Add file ← Upload files). مطمئن شوید پوشه‌ی `.github` هم آپلود شود. یا با گیت:
   ```
   git init && git add . && git commit -m init
   git branch -M main
   git remote add origin https://github.com/USER/goldscalp-apk.git
   git push -u origin main
   ```
3. به تب **Actions** بروید. ساخت خودکار شروع می‌شود (حدود ۳ تا ۶ دقیقه). اگر شروع نشد، `Build APK` را انتخاب و Run workflow را بزنید.
4. بعد از سبز شدن، وارد آن اجرا شوید و از بخش **Artifacts** فایل `GoldScalp-apk` را دانلود کنید. داخل zip فایل `app-debug.apk` است.
5. APK را به گوشی منتقل و نصب کنید. اگر اندروید پرسید، نصب از منبع ناشناخته را برای همان برنامه (مرورگر یا فایل‌منیجر) مجاز کنید.

## راه ۲: ساخت با Android Studio

پوشه را با Android Studio باز کنید، صبر کنید Gradle sync شود، سپس **Build ← Build Bundle(s) / APK(s) ← Build APK(s)**. فایل خروجی: `app/build/outputs/apk/debug/app-debug.apk`.

## نکات

- این APK با کلید دیباگ امضا شده و برای استفاده‌ی شخصی کافی است.
- اگر ساخت خطا داد، متن خطا را بفرستید تا درست شود.
- اپ فقط صفحه‌ی سرور شما را نشان می‌دهد؛ سرور (`gold_mobile`) باید روشن و در دسترس باشد.
