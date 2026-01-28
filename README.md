# Projekt_Aplikacje_Mobilne

Projekt Rejestrator Jazdy
Aplikacja mobilna na system Android służąca do monitorowania i rejestrowania parametrów jazdy w czasie rzeczywistym. Dzięki wykorzystaniu wbudowanych sensorów, aplikacja pozwala na podgląd obrazu z kamery z nałożonymi danymi o prędkości i przeciążeniach.

Opis Aplikacji
Rejestrator Jazdy to narzędzie typu "Dashcam" z rozszerzonymi funkcjami telemetrycznymi. Główne moduły aplikacji to:

Dashboard (Ekran główny): Podgląd na żywo z kamery urządzenia wraz z nakładką wyświetlającą aktualną prędkość (km/h) oraz dynamiczny wykres przeciążeń G-Force.

Rejestracja sesji: Możliwość rozpoczęcia i zakończenia zapisu trasy. Podczas zapisu aplikacja gromadzi dane o prędkości i przeciążeniach co sekundę.

Wykonywanie zdjęć: Przycisk "FOTO" pozwala na szybkie uchwycenie klatki z kamery i zapisanie jej w galerii urządzenia.

Historia i Szczegóły: Przeglądanie zapisanych sesji wraz z interaktywnymi wykresami liniowymi dla prędkości oraz przeciążeń dla każdej odbytej trasy.

Lista użytych sensorów i modułów
Aplikacja aktywnie korzysta z następujących komponentów sprzętowych:

Kamera (CameraX): Wykorzystywana do podglądu drogi i wykonywania zdjęć.

Moduł GPS (Fused Location Provider): Służy do precyzyjnego pomiaru prędkości pojazdu w czasie rzeczywistym.

Akcelerometr (TYPE_ACCELEROMETER): Odpowiada za obliczanie wypadkowej siły G (przeciążeń) działających na telefon.

Baza danych Room: Lokalne przechowywanie historii przejazdów i punktów pomiarowych.

Instrukcja uruchomienia
Aby uruchomić projekt lokalnie, wykonaj poniższe kroki:

Wymagania: Android Studio  oraz SDK w wersji 36.

Import: Otwórz Android Studio i wybierz opcję Open, a następnie wskaż folder ProjektRejestratorjazdy2.

Synchronizacja: Poczekaj na zakończenie synchronizacji plików Gradle (używany jest Kotlin DSL oraz Gradle 8.13).

Uprawnienia: Przy pierwszym uruchomieniu aplikacja poprosi o dostęp do Kamery oraz Lokalizacji (GPS). Są one niezbędne do poprawnego działania liczników i podglądu.

Uruchomienie: Podłącz fizyczne urządzenie z systemem Android (min. API 29) lub uruchom emulator i kliknij przycisk Run.

Zrzuty ekranu
![1769631981460](https://github.com/user-attachments/assets/c30bb1ad-d90b-46dc-9cdc-b2f4780b8fa9)
![1769631981494](https://github.com/user-attachments/assets/9696a3cd-ca3d-4e4f-8a3a-33a3316294f1)
![1769631981485](https://github.com/user-attachments/assets/b8c0732e-cf50-4bed-b0c9-05910ae6799b)
![1769631981471](https://github.com/user-attachments/assets/43cbeabc-c5fb-4794-ae62-2033d75ba38f)
