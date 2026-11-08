# LAB 08 — Mockito Mock объект ашиглан тестлэх

**Сургууль:** Шинжлэх Ухаан, Технологийн Их Сургууль  
**Оюутан:** Оюунсүрэн Мөнхзолбоо  
**Оюутны код:** B210910878

## 1. Ажлын зорилго

Энэхүү лабораторийн ажлаар Mockito ашиглан өгөгдлийн сан болон имэйл илгээх үйлчилгээг mock объектоор орлуулж, `CourseRegistrationService` классын ажиллагааг JUnit 5 ашиглан шалгав.

Мөн mutation testing хийж, тестүүд алдаатай кодыг илрүүлж чадаж байгаа эсэхийг туршив.  

## 2. Ашигласан орчин

- Үйлдлийн систем: Windows 11
- Java: OpenJDK 17.0.20.1 LTS (Microsoft)
- Maven: Apache Maven 3.9.16
- Тестийн сан: JUnit 5, Mockito


### Java хувилбар

Команд: `java -version`

```text
openjdk version "17.0.20.1" 2026-08-18 LTS
OpenJDK Runtime Environment Microsoft-14940689 (build 17.0.20.1+1-LTS)
OpenJDK 64-Bit Server VM Microsoft-14940689 (build 17.0.20.1+1-LTS, mixed mode, sharing)
```

### Maven хувилбар

Команд: `mvn -version`

```text
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\maven\apache-maven-3.9.16
Java version: 17.0.20.1, vendor: Microsoft, runtime: C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

## 3. Mockito тестүүд

| № | Тестийн зорилго | Үр дүн |
|---|---|---|
| A1 | Идэвхтэй оюутан амжилттай бүртгүүлэх | PASS |
| A2 | Байхгүй оюутны бүртгэлийг хориглох | PASS |
| A3 | Идэвхгүй оюутны бүртгэлийг хориглох | PASS |
| A4 | Өмнөх холбоо хичээл дутуу үед бүртгэхгүй байх | PASS |
| A5 | Имэйл илгээхэд алдаа гарсан ч бүртгэл амжилттай байх | PASS |
| A6 | Null утгатай оролтыг шалгах | PASS |
| A7 | ArgumentCaptor ашиглан имэйлийн агуулгыг шалгах | PASS |
| A8 | ParameterizedTest ашиглан 3 төрлийн алдааг шалгах | PASS |

A8 тест нь 3 өөр өгөгдлөөр ажилласан тул нийт **10 тестийн гүйцэтгэл** хийгдсэн.

### Тест ажиллуулах команд

`mvn test`

### Үр дүн

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Үр дүнгийн файл: `results/mvn-test.txt`

## 4. Mutation testing

Үндсэн кодод гурван төрлийн алдааг зориудаар оруулж, тестүүд илрүүлж байгаа эсэхийг шалгав.


| Mutant | Failed tests | Failures |
|---|---|---|

| 1 — Remove inactive-student check | `inactiveStudent_returnsErrorWithoutEmailInteraction`, `invalidRegistration_returnsExpectedError[2]` (INACTIVE_STUDENT) | 2 |
| 2 — Remove saveRegistration() | `activeStudent_validCourse_returnsOK`, `emailFailure_registrationStillSucceeds` | 2 |
| 3 — Remove courseId == null check | `nullInputs_throwIllegalArgumentException` | 1 |


Гурван өөрчлөлт тус бүрд `BUILD FAILURE` гарсан. Энэ нь тестүүд алдаатай кодыг илрүүлж байгааг харуулсан.

**Mutation detection: 3/3 (100%)**

Үр дүнгийн файлууд:

- `results/mvn-test-mutant-1.txt`
- `results/mvn-test-mutant-2.txt`
- `results/mvn-test-mutant-3.txt`

Mutation testing дууссаны дараа үндсэн кодыг сэргээж, 10 тестийг дахин амжилттай ажиллуулсан.

## 5. Mock болон Fake объектын ялгаа

Mock объект нь бодит хамаарлыг орлож, метод ямар утга буцаах болон ямар дуудлага хийгдсэнийг шалгах боломж олгодог. Харин Fake объект нь бодит системийн хялбаршуулсан хэрэгжүүлэлт байдаг. Жишээлбэл, Fake repository нь өгөгдлийг санах ойд хадгалж болно. Mockito ашигласнаар өгөгдлийн сан үүсгэх шаардлагагүйгээр тест бичих боломжтой. Mock нь методын дуудлагыг `verify()` ашиглан шалгахад тохиромжтой. Харин Fake нь олон үйлдлийн хамтын ажиллагааг шалгахад хэрэгтэй байж болно. Аль аргыг сонгох нь тестийн зорилгоос хамаарна.

## 6. Юуг mock хийх шаардлагагүй вэ?

`Student` зэрэг энгийн өгөгдлийн объектыг mock хийх шаардлагагүй. Учир нь ийм объектуудыг шууд үүсгэж, бодит утгаар нь шалгах боломжтой. Мөн тестэлж буй `CourseRegistrationService` классыг mock хийхгүй, жинхэнэ хэрэгжүүлэлтийг нь ажиллуулна. Харин өгөгдлийн сан, имэйл үйлчилгээ зэрэг гаднын хамаарлыг mock ашиглан орлуулна.

## 7. verify() ашиглахад анхаарах зүйл

`verify()` нь шаардлагатай метод дуудагдсан эсэхийг шалгахад хэрэгтэй. Гэхдээ хэт олон `verify()` ашиглавал тест кодын дотоод хэрэгжүүлэлтээс хэт хамааралтай болно. Ингэснээр зөв ажиллаж байгаа кодыг өөрчлөхөд тест шаардлагагүйгээр унах эрсдэлтэй. Тиймээс зөвхөн чухал үйлдлүүдийг шалгах нь тохиромжтой.

## 8. Дүгнэлт

Энэ лабораторийн ажлаар Mockito ашиглан хамаарлуудыг орлуулж, JUnit 5-аар нийт 10 тестийг амжилттай ажиллуулав. `when()`, `verify()`, `never()`, `verifyNoInteractions()`, `doThrow()` болон `ArgumentCaptor` ашиглаж сурсан. Мөн гурван mutation хийж, бүгдийг нь тестээр илрүүлсэн. Ингэснээр тест нь зөв ажиллагааг шалгахаас гадна алдааг илрүүлэх чадвартай байх ёстойг ойлгосон.