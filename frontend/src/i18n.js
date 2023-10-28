import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from 'i18next-browser-languagedetector';

// the translations
// (tip move them in a JSON file and import them,
// or even better, manage them separated from your code: https://react.i18next.com/guides/multiple-translation-files)
const resources = {
    de: {
        translation: {
            "chat.placeholder": "Hier könnte ihre Nachricht stehen..."
        }
    },
    en: {
        translation: {
            "chat.placeholder": "This could be your message..."
        }
    }
};

i18n
    .use(initReactI18next)
    .use(LanguageDetector)
    .init({
        resources,
        fallbackLng: "de",
        debug: true,
        interpolation: {
            escapeValue: false // react already handles this by default
        }
    });

export default i18n;