# Adventure Kingdom Economy — Forge 1.20.1

รุ่นพัฒนาต่อจาก Source ZIP ที่ผู้ใช้ส่งมา โดยกำหนดเป้าหมาย Minecraft Java 1.20.1 / Forge 47.4.28 / Java 17 และ Easy NPC 7.18.0

## สิ่งที่เพิ่มในรุ่นนี้

- SimpleChannel สำหรับสื่อสาร Client/Server
- Custom Medieval Fantasy-style screen สำหรับธนาคาร (ฝาก/ถอนครั้งละ 100 หรือ 500 Coin)
- คำสั่งเชื่อม NPC: `/ake npc bank|market|guild|shop`
- HUD แสดง Wallet และ Bank มุมซ้ายบน โดยรับยอดจาก Server ตอนผู้เล่นเข้าเกมและหลังทำธุรกรรม
- หน้าตลาด/กิลด์/ร้านค้าเป็นโครงหน้าจอพร้อมใช้งานต่อ แต่ยังไม่มีระบบรายการสินค้า/เควสผูกกับปุ่มใน GUI

## Build

ต้องใช้ JDK 17 (แนะนำ Temurin 17) และ Gradle 8.x ที่เข้ากันกับ ForgeGradle 6

จากโฟลเดอร์โปรเจกต์ `AdventureEconomy`:

```bash
gradle build
```

ไฟล์ JAR จะอยู่ใน `build/libs/AdventureEconomy-1.20.1-0.1.0.jar`

> ZIP นี้ไม่มี Gradle Wrapper และสภาพแวดล้อมที่ใช้จัดทำไฟล์ไม่มี Gradle executable จึงยังไม่ได้ยืนยันผล compile จริง กรุณา build ด้วย JDK 17 + Gradle 8.x ก่อนนำเข้าเซิร์ฟเวอร์

## ติดตั้ง

1. สำรองโลกและโฟลเดอร์ `mods` ก่อน
2. ใช้ Minecraft Java 1.20.1 และ Forge 47.4.28 ทั้งฝั่ง Client และ Server
3. ติดตั้ง Easy NPC 7.18.0 และ dependency ที่หน้าไฟล์ของ Easy NPC ระบุให้ครบ
4. Build โปรเจกต์ แล้วคัดลอก JAR จาก `build/libs/` ไปไว้ใน `mods` ของ CurseForge profile
5. เปิดเกม/เซิร์ฟเวอร์ ตรวจสอบ `latest.log` ว่า Adventure Economy โหลดสำเร็จ

## เชื่อม Easy NPC Dialog

สร้าง NPC แยกตามหน้าที่ แล้วเพิ่ม Dialog option/Action ชนิด Run Command (ชื่อเมนูอาจต่างกันตามหน้าต่างของ Easy NPC):

| NPC | คำสั่งที่ให้รัน |
|---|---|
| Banker | `execute as @p run ake npc bank` |
| Market Clerk | `execute as @p run ake npc market` |
| Guild Receptionist | `execute as @p run ake npc guild` |
| Merchant | `execute as @p run ake npc shop` |

ตั้งให้คำสั่งทำงานจากฝั่ง Server และให้ผู้เล่นที่กด Dialog เป็น executor/เป้าหมาย `@p` ต้องทดสอบระยะและ executor ใน Easy NPC 7.18.0 ของชุดม็อดจริงก่อนเปิดเซิร์ฟเวอร์สาธารณะ หาก Action ของ Easy NPC มีช่อง command โดยตรง ให้ใส่คำสั่งข้างต้นโดยไม่ต้องพิมพ์ `/` นำหน้า

## คำสั่งทดสอบ

- `/money` ดูยอดเงิน
- `/bank deposit <จำนวน>` ฝากเงิน
- `/bank withdraw <จำนวน>` ถอนเงิน
- `/ake npc bank` เปิด GUI ธนาคาร (ต้องเป็นผู้เล่น)
- `/ake npc market` เปิดหน้าตลาดโครงร่าง
- `/ake npc guild` เปิดหน้ากิลด์โครงร่าง
- `/ake npc shop` เปิดหน้าร้านค้าโครงร่าง

## ข้อจำกัด/งานถัดไป

- GUI ปัจจุบันเป็น milestone แรก: ธนาคารทำธุรกรรมได้จริง แต่หน้าตลาด/กิลด์/ร้านค้ายังเป็น placeholder
- HUD แสดงตำแหน่งคงที่มุมซ้ายบน ยังไม่มีหน้าตั้งค่าตำแหน่ง/ซ่อน HUD
- Player Trade แบบ escrow + ยืนยันสองฝ่ายยังไม่ได้เพิ่ม
- Housing claim/protection และการกำหนดสมาชิก/คลังส่วนตัวยังไม่ได้เพิ่ม
- Lucky Chest, auction escrow, minigame และระบบ permission อื่นต้อง audit เพิ่มก่อนใช้งานจริง
- Network packet ตรวจสอบจำนวนเงินฝั่ง Server และไม่เชื่อค่าจาก Client แต่ควรทดสอบ multiplayer จริงก่อน deploy


## ฟีเจอร์ที่เพิ่มในรอบถัดมา

- Market: `/market list`, `/market sell <price>`, `/market buy <id>` (เปิดร้านก่อนด้วย `/shop open`); GUI ตลาดมีปุ่มลัดสำหรับรายการ/ลงขาย/เปิดร้าน
- Guild: GUI มีปุ่มสมัครกิลด์และรับเควส `hunt_skeleton`; ใช้ `/quest list`, `/quest status`, `/quest claim` สำหรับจัดการเควส
- Trade: `/trade request <player>` → `/trade accept` → เสนอเงิน/ไอเทมด้วย `/trade offer money <amount>` หรือ `/trade offer hand` → ทั้งสองฝ่าย `/trade confirm`; `/trade cancel` หรือหมดเวลา 2 นาทีจะคืน escrow
- HUD: `/ake npc hud` เปิดหน้าปรับตำแหน่ง/ขนาด/ซ่อน HUD; บันทึกใน `config/adventureeconomy-hud.properties` ของ Client
- Housing: `/house claim <radius> <price>`, `/house trust <id> <player>`, `/house untrust <id> <player>`; ป้องกัน block break/place/right-click ในพื้นที่ที่มี claim

ข้อจำกัด: Market และ Guild GUI เป็น command shortcuts ไม่ใช่รายการแบบ inventory GUI; Trade ยังเป็น command-driven และแลกไอเทมได้หนึ่ง stack ต่อฝ่าย; Housing ยังต้องเพิ่มการป้องกัน entity/รถราง/ของเหลว/piston/fire/explosion และ block จากม็อดให้ครบถ้วน
