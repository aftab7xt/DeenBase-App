package com.deenbase.app.features.dhikr.ui

fun getBismillahContent(period: String) = DhikrContent(
    id = "bismillah",
    period = period,
    arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَىْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
    transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shai'un fil-ardi wa la fis-sama'i, wa Huwas-Sami'ul-'Alim",
    translation = "In the Name of Allah with Whose Name there is protection against every kind of harm in the earth or in the heaven, and He is the All-Hearing and All-Knowing.",
    hadithNarrator = "Narrated 'Uthman ibn 'Affan (may Allah be pleased with him):",
    hadithText = "He who recites three times every morning and evening: 'Bismillahil-ladhi la yadurru...' nothing will harm him.",
    hadithSource = "Abu Dawud • At-Tirmidhi • Riyad as-Salihin 1457",
    completionEmoji = "🤲",
    completionTitle = "You are protected",
    completionMessage = "Allah's Messenger ﷺ promised that nothing will harm you today. May Allah keep you and your family safe."
)

fun getRadituContent(period: String) = DhikrContent(
    id = "raditu",
    period = period,
    arabicText = "رَضِيتُ بِاللَّهِ رَبًّا وَبِالإِسْلاَمِ دِينًا وَبِمُحَمَّدٍ نَبِيًّا",
    transliteration = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin Nabiyyan",
    translation = "I am pleased with Allah as my Lord, Islam as my religion, and Muhammad ﷺ as Allah's Prophet and Messenger.",
    hadithNarrator = "Reference: Ahmed 4/337, An-Nasai (Hisnul Muslim), Abu Dawood Book 4 Hadith 318:",
    hadithText = "Allah has promised that anyone who says this three times every morning or evening will be pleased on the Day of Resurrection.",
    hadithSource = "Abu Dawood • At-Tirmidhi 5/465 • Ibn As-Sunni (no. 68)",
    completionEmoji = "✨",
    completionTitle = "Allah is pleased with you",
    completionMessage = "You have declared your contentment with Allah, Islam, and His Prophet ﷺ. May Allah make you among those who are pleased on the Day of Resurrection."
)
