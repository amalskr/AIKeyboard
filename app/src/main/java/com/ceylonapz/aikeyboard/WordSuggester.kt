package com.ceylonapz.aikeyboard

/**
 * Fast prefix-based word suggester using a pre-indexed dictionary
 * of common English words sorted by usage frequency.
 */
class WordSuggester {

    // Words grouped by first two characters for fast prefix lookup
    private val index: Map<String, List<String>>

    init {
        index = WORDS.groupBy { it.take(2) }
    }

    /**
     * Prefix completions for the word currently being typed.
     * "p" -> ["people", "put", "people"], "prep" -> ["prepare", "prepared"], etc.
     */
    fun suggest(prefix: String, limit: Int = 3): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val p = prefix.lowercase()
        return if (prefix.length == 1) {
            // Single-letter prefix: scan everything starting with that letter
            WORDS.asSequence()
                .filter { it.startsWith(p) && it != p }
                .take(limit)
                .toList()
        } else {
            val key = p.take(2)
            index[key]
                ?.filter { it.startsWith(p) && it != p }
                ?.take(limit)
                ?: emptyList()
        }
    }

    /**
     * Next-word suggestions to show when the user has just typed a space.
     * Looks up the previous word in a small bigram map; falls back to the most
     * common starter words if the previous word is unknown or empty.
     */
    fun suggestNext(previousWord: String, limit: Int = 3): List<String> {
        val p = previousWord.lowercase().trim()
        val bigrams = if (p.isNotEmpty()) NEXT_WORD[p] else null
        return (bigrams ?: COMMON_STARTERS).take(limit)
    }

    companion object {
        private val COMMON_STARTERS = listOf(
            "I", "the", "you", "do", "to", "a", "and", "is", "it", "that"
        )

        // Hand-picked bigram completions for high-frequency previous words.
        // Casing here is what gets inserted into the buffer when the chip is tapped.
        private val NEXT_WORD = mapOf(
            "i" to listOf("am", "have", "will", "do", "think", "want", "need"),
            "you" to listOf("are", "can", "will", "have", "should", "know"),
            "we" to listOf("are", "can", "will", "have", "should", "need"),
            "they" to listOf("are", "have", "will", "can", "were"),
            "he" to listOf("is", "was", "has", "will", "said"),
            "she" to listOf("is", "was", "has", "will", "said"),
            "it" to listOf("is", "was", "will", "would", "could"),
            "the" to listOf("best", "first", "next", "same", "new", "other"),
            "a" to listOf("lot", "little", "few", "new", "good"),
            "an" to listOf("hour", "idea", "issue", "answer", "email"),
            "to" to listOf("be", "do", "go", "see", "get", "make"),
            "for" to listOf("the", "you", "me", "us", "a", "now"),
            "of" to listOf("the", "course", "us", "them"),
            "and" to listOf("then", "I", "the", "we", "you"),
            "is" to listOf("a", "the", "not", "there", "it"),
            "was" to listOf("a", "the", "not", "going", "just"),
            "are" to listOf("you", "we", "they", "going", "not"),
            "do" to listOf("you", "not", "it", "this", "that"),
            "does" to listOf("it", "not", "that", "this"),
            "have" to listOf("a", "to", "you", "been", "the"),
            "has" to listOf("been", "a", "the", "to"),
            "will" to listOf("be", "do", "you", "have", "see"),
            "can" to listOf("you", "I", "we", "do", "be", "see"),
            "could" to listOf("you", "be", "have", "not"),
            "should" to listOf("be", "I", "you", "have", "not"),
            "would" to listOf("be", "you", "like", "have"),
            "let" to listOf("me", "us", "you", "it"),
            "going" to listOf("to", "on", "out", "home"),
            "want" to listOf("to", "you", "it", "a"),
            "need" to listOf("to", "you", "a", "more", "some"),
            "thank" to listOf("you", "you!", "you so"),
            "thanks" to listOf("for", "a lot", "again"),
            "see" to listOf("you", "the", "it", "what"),
            "what" to listOf("is", "are", "do", "you", "the"),
            "where" to listOf("are", "is", "did", "you"),
            "when" to listOf("you", "are", "is", "did", "the"),
            "how" to listOf("are", "is", "do", "you", "much"),
            "why" to listOf("is", "are", "did", "you", "do"),
            "good" to listOf("morning", "luck", "to", "for"),
            "happy" to listOf("birthday", "to", "with", "for"),
            "hi" to listOf("there", "everyone", "all"),
            "hey" to listOf("there", "you", "buddy", "everyone"),
            "hello" to listOf("there", "everyone", "all"),
        )

        // ~1000 common English words ordered by frequency within each prefix group
        private val WORDS = listOf(
            // a
            "a", "about", "above", "across", "act", "action", "activity", "actually",
            "add", "added", "address", "advance", "after", "afternoon", "again", "against",
            "age", "ago", "agree", "agreed", "ahead", "air", "airplane", "all", "allow",
            "allowed", "almost", "alone", "along", "already", "also", "always", "am",
            "amazing", "american", "among", "amount", "an", "and", "anger", "angry",
            "animal", "animals", "another", "answer", "answered", "any", "anybody",
            "anymore", "anyone", "anything", "anyway", "anywhere", "apart", "apartment",
            "app", "appear", "appeared", "apple", "application", "apply", "approach",
            "appropriate", "april", "are", "area", "aren't", "arm", "arms", "army",
            "around", "arrange", "arrive", "arrived", "art", "article", "as", "ask",
            "asked", "asleep", "at", "ate", "attack", "attention", "august", "available",
            "average", "avoid", "awake", "away", "awesome",
            // b
            "baby", "back", "bad", "bag", "ball", "band", "bank", "bar", "base",
            "basic", "basically", "bathroom", "be", "beach", "bear", "beat", "beautiful",
            "beauty", "became", "because", "become", "bed", "bedroom", "been", "before",
            "began", "begin", "beginning", "behind", "being", "believe", "believed",
            "below", "beside", "besides", "best", "better", "between", "beyond", "big",
            "bill", "billion", "bird", "birthday", "bit", "black", "block", "blog",
            "blood", "blow", "blue", "board", "boat", "body", "bone", "book", "born",
            "boss", "both", "bother", "bottle", "bottom", "bought", "box", "boy",
            "boyfriend", "brain", "bread", "break", "breakfast", "breath", "breathe",
            "bright", "bring", "broad", "broke", "broken", "brother", "brought", "brown",
            "budget", "build", "building", "built", "burn", "bus", "business", "busy",
            "but", "buy", "by",
            // c
            "call", "called", "calm", "came", "camera", "camp", "campaign", "can",
            "canada", "cancel", "car", "card", "care", "career", "careful", "carefully",
            "carry", "case", "cash", "cat", "catch", "caught", "cause", "cell", "center",
            "central", "certain", "certainly", "chair", "challenge", "chance", "change",
            "changed", "channel", "character", "charge", "check", "chest", "chicken",
            "child", "children", "china", "choice", "choose", "church", "city", "claim",
            "class", "clean", "clear", "clearly", "client", "climb", "close", "closed",
            "clothes", "cloud", "club", "coach", "code", "coffee", "cold", "collect",
            "college", "color", "come", "comfortable", "coming", "comment", "common",
            "community", "company", "compare", "complete", "completely", "computer",
            "concern", "concerned", "condition", "conference", "confirm", "connect",
            "consider", "contact", "content", "continue", "control", "conversation",
            "cook", "cool", "copy", "corner", "correct", "cost", "could", "couldn't",
            "count", "country", "couple", "course", "court", "cover", "covered", "crazy",
            "create", "credit", "cross", "crowd", "cry", "cup", "current", "currently",
            "customer", "cut",
            // d
            "dad", "daily", "damage", "dance", "danger", "dangerous", "dark", "data",
            "date", "daughter", "day", "days", "dead", "deal", "dear", "death", "december",
            "decide", "decided", "decision", "deep", "definitely", "degree", "deliver",
            "demand", "department", "describe", "design", "desk", "despite", "detail",
            "develop", "development", "device", "did", "didn't", "die", "died",
            "difference", "different", "difficult", "digital", "dinner", "direct",
            "direction", "director", "dirty", "disappear", "discover", "discuss",
            "discussion", "distance", "do", "doctor", "document", "does", "doesn't",
            "dog", "dollar", "dollars", "don't", "done", "door", "doubt", "down",
            "downtown", "draw", "dream", "dress", "drink", "drive", "driver", "drop",
            "dropped", "drug", "dry", "during",
            // e
            "each", "ear", "early", "earn", "earth", "easily", "east", "easy", "eat",
            "edge", "education", "effect", "effort", "eight", "either", "election",
            "element", "else", "email", "emergency", "emotion", "employee", "empty",
            "encourage", "end", "ended", "enemy", "energy", "engine", "enjoy", "enough",
            "enter", "entire", "environment", "episode", "equal", "equipment", "escape",
            "especially", "essential", "establish", "even", "evening", "event", "eventually",
            "ever", "every", "everybody", "everyone", "everything", "everywhere",
            "evidence", "evil", "exact", "exactly", "exam", "example", "excellent",
            "except", "exchange", "excited", "exciting", "excuse", "exercise", "exist",
            "expect", "expected", "expensive", "experience", "explain", "express",
            "extra", "eye", "eyes",
            // f
            "face", "fact", "fail", "fair", "faith", "fall", "familiar", "family",
            "famous", "fan", "far", "farm", "fast", "fat", "father", "fault", "favorite",
            "fear", "feature", "february", "feed", "feel", "feeling", "feet", "fell",
            "fellow", "felt", "female", "few", "field", "fight", "figure", "fill",
            "film", "final", "finally", "financial", "find", "fine", "finger", "finish",
            "finished", "fire", "firm", "first", "fish", "fit", "five", "fix", "flat",
            "flight", "floor", "flow", "fly", "focus", "follow", "following", "food",
            "foot", "for", "force", "foreign", "forest", "forever", "forget", "form",
            "former", "forward", "found", "four", "free", "freedom", "french", "fresh",
            "friday", "friend", "friends", "from", "front", "fruit", "fuel", "full",
            "fun", "function", "fund", "funny", "future",
            // g
            "gain", "game", "garden", "gas", "gate", "gather", "gave", "general",
            "generation", "gentleman", "get", "getting", "gift", "girl", "girlfriend",
            "give", "given", "glad", "glass", "global", "go", "goal", "god", "goes",
            "going", "gold", "gone", "good", "google", "got", "government", "grab",
            "grade", "grand", "grandfather", "grandmother", "grant", "grass", "gray",
            "great", "green", "grew", "ground", "group", "grow", "growing", "growth",
            "guard", "guess", "guide", "gun", "guy", "guys",
            // h
            "had", "hadn't", "hair", "half", "hall", "hand", "handle", "hang", "happen",
            "happened", "happy", "hard", "hardly", "has", "hasn't", "hat", "hate",
            "have", "haven't", "he", "head", "health", "healthy", "hear", "heard",
            "heart", "heat", "heavy", "held", "hell", "hello", "help", "her", "here",
            "herself", "hey", "hi", "hide", "high", "highly", "hill", "him", "himself",
            "his", "history", "hit", "hold", "hole", "holiday", "home", "honest",
            "honor", "hope", "horse", "hospital", "host", "hot", "hotel", "hour",
            "hours", "house", "household", "how", "however", "huge", "human", "humor",
            "hundred", "hung", "hungry", "hurt", "husband",
            // i
            "idea", "identify", "if", "ignore", "ill", "image", "imagine", "immediate",
            "immediately", "impact", "important", "impossible", "impression", "improve",
            "in", "incident", "include", "including", "increase", "indeed", "indicate",
            "individual", "industry", "influence", "information", "initial", "inside",
            "instead", "interest", "interested", "interesting", "international",
            "internet", "interview", "into", "introduce", "invest", "investigation",
            "investment", "involve", "involved", "is", "island", "isn't", "issue",
            "it", "item", "its", "itself",
            // j
            "january", "job", "jobs", "join", "joke", "journal", "journey", "joy",
            "judge", "july", "jump", "june", "junior", "just", "justice",
            // k
            "keep", "kept", "key", "kid", "kids", "kill", "killed", "kind", "king",
            "kitchen", "knee", "knew", "knock", "know", "knowledge", "known",
            // l
            "lack", "lady", "laid", "lake", "land", "language", "large", "last", "late",
            "later", "latest", "laugh", "laughed", "launch", "law", "lay", "lead",
            "leader", "leadership", "learn", "learned", "least", "leave", "led", "left",
            "leg", "legal", "less", "lesson", "let", "letter", "level", "library",
            "lie", "life", "lift", "light", "like", "likely", "limit", "line", "link",
            "list", "listen", "lit", "little", "live", "living", "local", "lock", "long",
            "longer", "look", "looked", "lord", "lose", "loss", "lost", "lot", "lots",
            "loud", "love", "lovely", "low", "lower", "luck", "lucky", "lunch",
            // m
            "machine", "mad", "made", "magazine", "magic", "main", "maintain", "major",
            "majority", "make", "making", "male", "man", "manage", "manager",
            "management", "many", "map", "march", "mark", "market", "marriage",
            "married", "master", "match", "material", "matter", "may", "maybe", "me",
            "meal", "mean", "meaning", "means", "measure", "media", "medical", "meet",
            "meeting", "member", "memory", "men", "mental", "mention", "mentioned",
            "message", "met", "method", "middle", "might", "mile", "military", "million",
            "mind", "mine", "minister", "minute", "minutes", "mirror", "miss", "missing",
            "mission", "mistake", "model", "modern", "mom", "moment", "monday", "money",
            "month", "months", "mood", "moon", "more", "morning", "most", "mostly",
            "mother", "mouth", "move", "moved", "movement", "movie", "mr", "mrs",
            "much", "murder", "museum", "music", "must", "my", "myself", "mystery",
            // n
            "name", "named", "nation", "national", "natural", "nature", "near",
            "nearly", "necessary", "neck", "need", "needed", "negative", "neighbor",
            "neither", "network", "never", "new", "news", "newspaper", "next", "nice",
            "night", "nine", "no", "nobody", "nod", "nodded", "noise", "none", "nor",
            "normal", "normally", "north", "nose", "not", "note", "noted", "nothing",
            "notice", "noticed", "november", "now", "nowhere", "number",
            // o
            "object", "obviously", "occur", "october", "of", "off", "offer", "offered",
            "office", "officer", "official", "often", "oh", "oil", "ok", "okay", "old",
            "on", "once", "one", "online", "only", "onto", "open", "opened", "opening",
            "operation", "opinion", "opportunity", "option", "or", "order", "ordered",
            "organization", "original", "other", "others", "otherwise", "our", "out",
            "outside", "over", "overall", "own", "owner",
            // p
            "package", "page", "paid", "pain", "paint", "pair", "paper", "parent",
            "parents", "park", "part", "particular", "particularly", "partner", "party",
            "pass", "passed", "past", "path", "patient", "pattern", "pause", "pay",
            "payment", "peace", "people", "per", "percent", "perfect", "perfectly",
            "perform", "performance", "perhaps", "period", "permit", "person", "personal",
            "personally", "phone", "photo", "phrase", "physical", "pick", "picked",
            "picture", "piece", "pilot", "place", "plan", "plane", "planet", "planned",
            "planning", "plant", "platform", "play", "player", "playing", "please",
            "pleasure", "plenty", "plus", "pocket", "poem", "point", "pointed", "police",
            "policy", "political", "politics", "poor", "popular", "population",
            "position", "positive", "possible", "possibly", "post", "potential", "pound",
            "power", "powerful", "practice", "prefer", "prepare", "prepared", "present",
            "president", "press", "pressure", "pretty", "prevent", "previous", "price",
            "private", "probably", "problem", "process", "produce", "product",
            "production", "professional", "professor", "profit", "program", "progress",
            "project", "promise", "proper", "property", "protect", "protection", "prove",
            "provide", "public", "pull", "pulled", "purchase", "purpose", "push",
            "pushed", "put", "putting",
            // q
            "quality", "quarter", "queen", "question", "questions", "quick", "quickly",
            "quiet", "quietly", "quit", "quite", "quote",
            // r
            "race", "radio", "rain", "raise", "raised", "ran", "range", "rapid",
            "rate", "rather", "reach", "reached", "react", "reaction", "read", "reading",
            "ready", "real", "reality", "realize", "realized", "really", "reason",
            "reasonable", "receive", "received", "recent", "recently", "recognize",
            "recommend", "record", "red", "reduce", "reflect", "reform", "regard",
            "region", "related", "relationship", "release", "religion", "remain",
            "remember", "remind", "remote", "remove", "repeat", "replace", "reply",
            "report", "represent", "republican", "request", "require", "required",
            "research", "resource", "respect", "respond", "response", "rest",
            "restaurant", "result", "return", "reveal", "review", "rich", "ride",
            "right", "ring", "rise", "risk", "river", "road", "rock", "role", "roll",
            "room", "rose", "rough", "round", "row", "rule", "run", "running", "rush",
            // s
            "sad", "safe", "safety", "said", "sale", "same", "sat", "saturday", "save",
            "saved", "saw", "say", "saying", "scale", "scene", "school", "science",
            "screen", "sea", "search", "season", "seat", "second", "secret", "section",
            "security", "see", "seek", "seem", "seemed", "seen", "select", "sell",
            "send", "senior", "sense", "sent", "sentence", "separate", "september",
            "series", "serious", "seriously", "serve", "service", "session", "set",
            "setting", "settle", "settled", "seven", "several", "share", "she", "ship",
            "shirt", "shock", "shoot", "shop", "shopping", "short", "shot", "should",
            "shoulder", "shouldn't", "shout", "show", "showed", "shut", "sick", "side",
            "sight", "sign", "signal", "significant", "silence", "silent", "similar",
            "simple", "simply", "since", "sing", "single", "sir", "sister", "sit",
            "site", "situation", "six", "size", "skill", "skin", "sky", "sleep", "slide",
            "slightly", "slip", "slow", "slowly", "small", "smart", "smell", "smile",
            "smiled", "smoke", "snow", "so", "social", "society", "soft", "software",
            "sold", "soldier", "solution", "some", "somebody", "somehow", "someone",
            "something", "sometimes", "somewhat", "somewhere", "son", "song", "soon",
            "sorry", "sort", "soul", "sound", "source", "south", "southern", "space",
            "speak", "special", "specific", "speech", "speed", "spend", "spent",
            "spirit", "spoke", "sport", "sports", "spot", "spread", "spring", "staff",
            "stage", "stand", "standard", "standing", "star", "stare", "stared", "start",
            "started", "state", "statement", "station", "stay", "stayed", "step",
            "stick", "still", "stock", "stomach", "stone", "stood", "stop", "stopped",
            "store", "storm", "story", "straight", "strange", "strategy", "street",
            "strength", "stress", "strike", "strong", "strongly", "structure", "struggle",
            "student", "study", "stuff", "stupid", "style", "subject", "success",
            "successful", "such", "suddenly", "suffer", "suggest", "suggestion",
            "summer", "sun", "sunday", "support", "suppose", "sure", "surely",
            "surprise", "surprised", "surround", "sweet", "swim", "switch", "system",
            // t
            "table", "tail", "take", "taken", "tale", "talk", "talked", "tall", "task",
            "taste", "tax", "teach", "teacher", "team", "tear", "technology", "tell",
            "ten", "tend", "term", "terms", "terrible", "test", "text", "than", "thank",
            "thanks", "that", "the", "their", "them", "themselves", "then", "theory",
            "there", "therefore", "these", "they", "thick", "thin", "thing", "things",
            "think", "thinking", "third", "this", "those", "though", "thought",
            "thousand", "threat", "three", "threw", "through", "throughout", "throw",
            "thursday", "ticket", "tie", "tight", "till", "time", "tiny", "tip", "tire",
            "tired", "title", "to", "today", "together", "told", "tomorrow", "tone",
            "tonight", "too", "took", "tool", "top", "topic", "total", "totally",
            "touch", "touched", "tough", "tour", "toward", "towards", "town", "track",
            "trade", "tradition", "traditional", "traffic", "train", "training",
            "transfer", "travel", "treat", "tree", "trial", "trick", "tried", "trip",
            "trouble", "truck", "true", "truly", "trust", "truth", "try", "trying",
            "tuesday", "turn", "turned", "tv", "twelve", "twenty", "twice", "two",
            "type", "typical", "typically",
            // u
            "ugly", "uncle", "under", "understand", "understanding", "unfortunately",
            "union", "unique", "unit", "united", "university", "unless", "unlike",
            "unlikely", "until", "up", "upon", "upper", "upset", "urban", "us", "use",
            "used", "useful", "user", "using", "usual", "usually",
            // v
            "valley", "value", "van", "variety", "various", "vehicle", "version",
            "very", "via", "victim", "video", "view", "village", "violence", "visit",
            "voice", "volume", "vote",
            // w
            "wait", "waited", "wake", "walk", "walked", "wall", "want", "wanted",
            "war", "warm", "warn", "warning", "was", "wash", "wasn't", "waste", "watch",
            "watching", "water", "wave", "way", "we", "weak", "weapon", "wear",
            "weather", "website", "wednesday", "week", "weekend", "weeks", "weight",
            "welcome", "well", "went", "were", "weren't", "west", "western", "what",
            "whatever", "when", "where", "wherever", "whether", "which", "while",
            "whisper", "white", "who", "whole", "whom", "whose", "why", "wide", "wife",
            "wild", "will", "willing", "win", "wind", "window", "wine", "wing",
            "winter", "wish", "with", "within", "without", "woke", "woman", "women",
            "won", "wonder", "wonderful", "won't", "wood", "word", "words", "wore",
            "work", "worked", "worker", "working", "works", "world", "worried", "worry",
            "worse", "worst", "worth", "would", "wouldn't", "wow", "write", "writer",
            "writing", "written", "wrong", "wrote",
            // y
            "yard", "yeah", "year", "years", "yell", "yellow", "yes", "yesterday",
            "yet", "you", "young", "younger", "your", "yours", "yourself", "youth",
            // z
            "zero", "zone"
        )
    }
}
