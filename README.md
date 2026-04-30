# Restaurant Management System (Restoran Boshqaruv Tizimi)

Ushbu loyiha restoran faoliyatini avtomatlashtirish va samarali boshqarish uchun mo'ljallangan zamonaviy desktop ilovadir. Loyiha Java va JavaFX texnologiyalari asosida qurilgan bo'lib, keng qamrovli funksionallikka ega.

## 🚀 Asosiy Imkoniyatlar (Features)

Loyiha foydalanuvchilarning rollariga qarab turlicha imkoniyatlarni taqdim etadi:

### 🎭 Rollarga asoslangan kirish (RBAC)
*   **Manager (Menejer):** Tizimning barcha qismlarini boshqarish huquqi (xodimlar, mijozlar, menyu, stollar, buyurtmalar, to'lovlar).
*   **Waiter (Ofitsiant):** Buyurtma olish va mijozlar bilan ishlash.
*   **Receptionist (Administrator):** Mijozlarni kutib olish va stollarni band qilish (reservations).
*   **Chef (Oshpaz):** Buyurtmalar holatini kuzatish va taom tayyorlash jarayoni.
*   **Cashier (Kassir):** To'lovlarni qabul qilish va cheklarni boshqarish.

### 📊 Menejer Paneli (Manager Dashboard)
*   **Statistika:** Xodimlar soni, mijozlar, menyu elementlari, band qilingan joylar va faol buyurtmalar haqida real vaqt rejimida ma'lumot.
*   **Xodimlarni Boshqarish:** Xodimlarni qo'shish, tahrirlash va o'chirish.
*   **Mijozlar Bazasi:** Mijozlar ma'lumotlarini saqlash va boshqarish.
*   **Menyu Boshqaruvi:** Taomlar va ichimliklarni rasmlari bilan qo'shish, narxlarni belgilash.
*   **Stollar va Band qilish:** Restoran stollari holatini kuzatish va rezervatsiyalarni boshqarish.
*   **Buyurtma va To'lovlar:** Barcha buyurtmalarni kuzatish va moliyaviy amallarni (naqd, karta orqali) boshqarish.

### 🍱 Ofitsiant Paneli (Waiter Dashboard)
*   Tezkor buyurtma olish interfeysi.
*   Stollar holatini ko'rish.
*   Mijozlar ma'lumotlarini kiritish.

## 🛠 Texnologiyalar (Technologies)

*   **Dasturlash tili:** Java 21
*   **UI Framework:** JavaFX 21
*   **Ma'lumotlar bazasi:** MySQL
*   **Build Tool:** Maven
*   **Uslublar (Styling):** CSS (JavaFX style sheets)
*   **Ulanishlar:** MySQL Connector/J

## 📂 Loyiha Tuzilishi (Project Structure)

```text
src/main/java/com/example/restaurantmanagementsystem/
├── Controller/    # UI mantiqini boshqaruvchi klasslar
├── DAO/           # Ma'lumotlar bazasi bilan ishlash (Database Access Objects)
├── Enums/         # Tizimda ishlatiladigan doimiy qiymatlar (Statuslar, Rollar)
├── Model/         # Ma'lumotlar modellari (POJO klasslar)
├── config/        # Konfiguratsiya fayllari
├── db/            # Ma'lumotlar bazasiga ulanish mantiqi
├── repository/    # Ma'lumotlarni saqlash va olish qatlami
├── service/       # Biznes mantiq qatlami
└── HelloApplication.java  # Ilovani ishga tushirish nuqtasi

src/main/resources/com/example/restaurantmanagementsystem/
├── AddMenu.fxml           # Menyu qo'shish oynasi
├── ManagerDashboard.fxml  # Menejer paneli interfeysi
├── WaiterDashboard.fxml   # Ofitsiant paneli interfeysi
├── login.fxml             # Kirish oynasi
└── style.css              # Dizayn va uslublar
```

## ⚙️ O'rnatish va Ishga tushirish

### Talablar:
1.  **JDK 21** yoki undan yuqori versiya.
2.  **MySQL Server** o'rnatilgan va ishga tushirilgan bo'lishi kerak.
3.  **Maven** loyihani yig'ish uchun.

### Qadamlar:

1.  **Repository-ni klonlash:**
    ```bash
    git clone https://github.com/username/restaurant-management-system.git
    cd restaurant-management-system
    ```

2.  **Ma'lumotlar bazasini sozlash:**
    *   MySQL-da yangi ma'lumotlar bazasi yarating.
    *   `src/main/java/com/example/restaurantmanagementsystem/db/` ichidagi sozlamalarda (yoki mos konfiguratsiya faylida) bazaga ulanish ma'lumotlarini (username, password) ko'rsating.
    *   Tizim birinchi marta ishga tushganda jadvallar avtomatik yaratilishi mumkin (`DatabaseInitializer` orqali).

3.  **Loyihani yig'ish:**
    ```bash
    mvn clean install
    ```

4.  **Ishga tushirish:**
    ```bash
    mvn javafx:run
    ```

## 🎨 Dizayn
Loyiha zamonaviy va foydalanuvchi uchun qulay interfeysga ega. JavaFX CSS orqali ranglar palitrasi va elementlar ko'rinishi yuqori darajada optimallashtirilgan. Menyudagi taomlar rasmlari vizual tarzda ko'rinib turadi, bu esa foydalanishni yanada osonlashtiradi.

---
© 2026 Restaurant Management System. Barcha huquqlar himoyalangan.
