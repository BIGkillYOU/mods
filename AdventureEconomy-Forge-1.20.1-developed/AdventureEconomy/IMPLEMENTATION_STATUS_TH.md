# สถานะการพัฒนา Adventure Kingdom Economy

เป้าหมาย: Minecraft Java 1.20.1 / Forge 47.4.28 / Java 17

## ทำงานแล้วใน Source รอบนี้
- [x] Network Client/Server และ Sync เงิน
- [x] Bank Custom GUI ฝาก/ถอน
- [x] NPC Command Bridge: `/ake npc bank|market|guild|shop`
- [x] HUD Wallet/Bank พร้อมปรับตำแหน่ง/ขนาด/ซ่อน และบันทึกค่าในไฟล์ config ฝั่ง Client
- [x] Market backend: ลงขายไอเทมจากมือ, ดูรายการ, ซื้อ, ภาษีเข้าคลังเมือง (GUI เป็นปุ่มลัดคำสั่ง)
- [x] Guild GUI ปุ่มสมัคร/ดูเควส/รับเควส และคำสั่งสถานะ/ออกกิลด์
- [x] Player Trade backend: request/accept, escrow เงินและไอเทมหนึ่ง stack ต่อฝ่าย, confirm สองฝ่าย, cancel, timeout 2 นาที และคืน escrow เมื่อผู้เล่นออก
- [x] Housing claim แบบรัศมี, ตรวจพื้นที่ทับซ้อน, trust/untrust สมาชิก
- [x] Housing protection สำหรับ break/place/right-click block; เจ้าของ/สมาชิก/แอดมินผ่านได้

## ข้อจำกัดที่ยังเหลือ
- Market GUI ยังเป็น command-shortcut UI ไม่ใช่รายการสินค้าแบบ paginated/inventory GUI; ซื้อสินค้าด้วย `/market buy <id>`.
- Player Trade ใช้คำสั่ง ยังไม่มีหน้าต่าง Trade GUI และรองรับไอเทมหนึ่ง stack ต่อผู้เล่นในข้อเสนอ.
- Housing protection ครอบคลุม block break/place/right-click block แต่ยังไม่ครอบคลุม entity interaction, container minecart, fluid, piston, fire spread และทุกชนิดของ explosion/modded block.
- ระบบประมูลเดิมยังควร audit escrow ก่อนเปิดใช้งานจริง.
- ต้องทดสอบ multiplayer จริงและ build ด้วย ForgeGradle ก่อน deploy.

## คำสั่งสำคัญ
- `/ake npc bank|market|guild|shop` เปิด GUI ตาม NPC
- `/ake npc hud` เปิดการตั้งค่า HUD (Client-local)
- `/market list`, `/market sell <price>`, `/market buy <id>`; ก่อนลงขายให้เปิดร้านด้วย `/shop open`
- `/guild join`, `/guild status`, `/guild leave`
- `/quest list`, `/quest accept hunt_skeleton|gather_iron`, `/quest status`, `/quest claim`
- `/trade request <player>` → ผู้รับใช้ `/trade accept`; เสนอเงิน `/trade offer money <จำนวน>`; เสนอไอเทมในมือ `/trade offer hand`; ทั้งสองฝ่ายใช้ `/trade confirm`; ยกเลิก `/trade cancel`
- `/house claim <radius 4-32> <price>`; `/house trust <houseId> <player>`; `/house untrust <houseId> <player>`; `/house list`

## Build verification
ZIP ต้นฉบับไม่มี Gradle Wrapper และ environment ที่ใช้แก้ไขไม่มี Gradle executable จึงยังไม่ได้ยืนยัน compile ด้วย ForgeGradle ในรอบนี้ ใช้ JDK 17 + Gradle 8.x แล้วรัน `gradle build` จากโฟลเดอร์โปรเจกต์ก่อนนำเข้าเซิร์ฟเวอร์
