/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Emotions utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://hacpai.com/member/ZephyrJung">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Vanessa</a>
 * @version 1.3.0.7, Apr 27, 2020
 * @since 0.2.0
 */
public final class Emotions {

    /**
     * Emoji pattern.
     */
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":.+:");

    /**
     * Determines whether the specified string is a emoji or not.
     *
     * @param string the specified string
     * @return {@code true} if it is a emoji, returns {@code false} otherwise
     */
    public static boolean isEmoji(final String string) {
        for (final String emoji : EMOJIS) {
            if (emoji.equals(string)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Replaces the emoji's alias by its unicode. Example: ":smile:" gives "😄".
     *
     * @param content the specified string to parse
     * @return the string with the mojis replaces by their unicode
     */
    public static String toUnicode(String content) {
        // 该 Emoji 和 EmojiParser 命名不一致，此处做修正
        content = StringUtils.replace(content, ":unicorn:", ":unicorn_face:");
        content = StringUtils.replace(content, ":upside_down_face:", ":upside_down:");

        String ret = EmojiParser.parseToUnicode(content);
        ret = ret.replace("❤", "❤️");
        ret = ret.replace("♥", "♥️");

        return ret;
    }

    /**
     * Replaces the emoji's unicode occurrences by one of their alias (between 2 ':'). Example: "😄" gives ":smile:".
     *
     * @param content the string to parse
     * @return the string with the emojis replaced by their alias.
     */
    public static String toAliases(final String content) {
        return EmojiParser.parseToAliases(content);
    }

    /**
     * Clears the emotions ({@literal [em00], :heart:}) with specified content.
     *
     * @param content the specified content
     * @return cleared content
     */
    public static String clear(final String content) {
        String ret = content.replaceAll("\\[em\\d+]", "");
        for (final String emojiCode : EMOJIS) {
            final String emoji = ":" + emojiCode + ":";
            ret = ret.replace(emoji, "");
        }

        return ret;
    }

    /**
     * Converts the specified content with emotions. Replaces the emoji's alias by its unicode. Example: ":smile:" gives "😄".
     *
     * @param content the specified content
     * @return converted content
     */
    public static String convert(final String content) {
        String ret = content;
        if (!EMOJI_PATTERN.matcher(ret).find()) {
            return ret;
        }

        ret = toUnicode(ret);
        for (final String emojiCode : EMOJIS) {
            String repl = "<img alt=\"" + emojiCode + "\" class=\"emoji\" src=\"https://cdn.jsdelivr.net/npm/vditor/dist/images/emoji/" + emojiCode;
            final String suffix = "huaji".equals(emojiCode) ? ".gif" : ".png";
            repl += suffix + "\" title=\"" + emojiCode + "\" />";
            ret = ret.replace(":" + emojiCode + ":", repl);
        }

        return ret;
    }

    public static void main(String[] args) {
        String str1 = "Here is a boy: \uD83D\uDC66\uD83C\uDFFF!";
        String str2 = EmojiParser.parseToAliases(str1, EmojiParser.FitzpatrickAction.REMOVE);
    }

    /**
     * Emoji list.
     */
    private static final String[] EMOJIS = {
            "100", "1234", "+1", "-1", "1st_place_medal", "2nd_place_medal", "3rd_place_medal", "8ball", "a", "ab", "abc", "abcd", "accept", "aerial_tramway", "afghanistan", "airplane", "aland_islands", "alarm_clock", "albania", "alembic", "algeria", "alien", "ambulance", "american_samoa", "amphora", "anchor", "andorra", "angel", "anger", "angola", "angry", "anguilla", "anguished", "ant", "antarctica", "antigua_barbuda", "apple", "aquarius", "argentina", "aries", "armenia", "arrow_backward", "arrow_double_down", "arrow_double_up", "arrow_down", "arrow_down_small", "arrow_forward", "arrow_heading_down", "arrow_heading_up", "arrow_left", "arrow_lower_left", "arrow_lower_right", "arrow_right", "arrow_right_hook", "arrow_up", "arrow_up_down", "arrow_up_small", "arrow_upper_left", "arrow_upper_right", "arrows_clockwise", "arrows_counterclockwise", "art", "articulated_lorry", "artificial_satellite", "aruba", "asterisk", "astonished", "athletic_shoe", "atm", "atom_symbol", "australia", "austria", "avocado", "azerbaijan", "b", "b3log", "baby", "baby_bottle", "baby_chick", "baby_symbol", "back", "bacon", "badminton", "baggage_claim", "baguette_bread", "bahamas", "bahrain", "balance_scale", "balloon", "ballot_box", "ballot_box_with_check", "bamboo", "banana", "bangbang", "bangladesh", "bank", "bar_chart", "barbados", "barber", "baseball", "basketball", "basketball_man", "basketball_woman", "bat", "bath", "bathtub", "battery", "beach_umbrella", "bear", "bed", "bee", "beer", "beers", "beetle", "beginner", "belarus", "belgium", "belize", "bell", "bellhop_bell", "benin", "bento", "bermuda", "bhutan", "bicyclist", "bike", "biking_man", "biking_woman", "bikini", "biohazard", "bird", "birthday", "black_circle", "black_flag", "black_heart", "black_joker", "black_large_square", "black_medium_small_square", "black_medium_square", "black_nib", "black_small_square", "black_square_button", "blonde_man", "blonde_woman", "blossom", "blowfish", "blue_book", "blue_car", "blue_heart", "blush", "boar", "boat", "bolivia", "bomb", "book", "bookmark", "bookmark_tabs", "books", "boom", "boot", "bosnia_herzegovina", "botswana", "bouquet", "bow", "bow_and_arrow", "bowing_man", "bowing_woman", "bowling", "boxing_glove", "boy", "brazil", "bread", "bride_with_veil", "bridge_at_night", "briefcase", "british_indian_ocean_territory", "british_virgin_islands", "broken_heart", "brunei", "bug", "building_construction", "bulb", "bulgaria", "bullettrain_front", "bullettrain_side", "burkina_faso", "burrito", "burundi", "bus", "business_suit_levitating", "busstop", "bust_in_silhouette", "busts_in_silhouette", "butterfly", "cactus", "cake", "calendar", "call_me_hand", "calling", "cambodia", "camel", "camera", "camera_flash", "cameroon", "camping", "canada", "canary_islands", "cancer", "candle", "candy", "canoe", "cape_verde", "capital_abcd", "capricorn", "car", "card_file_box", "card_index", "card_index_dividers", "caribbean_netherlands", "carousel_horse", "carrot", "cat", "cat2", "cayman_islands", "cd", "central_african_republic", "chad", "chainbook", "chains", "champagne", "chart", "chart_with_downwards_trend", "chart_with_upwards_trend", "checkered_flag", "cheese", "cherries", "cherry_blossom", "chestnut", "chicken", "children_crossing", "chile", "chipmunk", "chocolate_bar", "christmas_island", "christmas_tree", "church", "cinema", "circus_tent", "city_sunrise", "city_sunset", "cityscape", "cl", "clamp", "clap", "clapper", "classical_building", "clinking_glasses", "clipboard", "clock1", "clock10", "clock1030", "clock11", "clock1130", "clock12", "clock1230", "clock130", "clock2", "clock230", "clock3", "clock330", "clock4", "clock430", "clock5", "clock530", "clock6", "clock630", "clock7", "clock730", "clock8", "clock830", "clock9", "clock930", "closed_book", "closed_lock_with_key", "closed_umbrella", "cloud", "cloud_with_lightning", "cloud_with_lightning_and_rain", "cloud_with_rain", "cloud_with_snow", "clown_face", "clubs", "cn", "cocktail", "cocos_islands", "coffee", "coffin", "cold_sweat", "collision", "colombia", "comet", "comoros", "computer", "computer_mouse", "confetti_ball", "confounded", "confused", "congo_brazzaville", "congo_kinshasa", "congratulations", "construction", "construction_worker", "construction_worker_man", "construction_worker_woman", "control_knobs", "convenience_store", "cook_islands", "cookie", "cool", "cop", "copyright", "corn", "costa_rica", "cote_divoire", "couch_and_lamp", "couple", "couple_with_heart", "couple_with_heart_man_man", "couple_with_heart_woman_man", "couple_with_heart_woman_woman", "couplekiss_man_man", "couplekiss_man_woman", "couplekiss_woman_woman", "cow", "cow2", "cowboy_hat_face", "crab", "crayon", "credit_card", "crescent_moon", "cricket", "croatia", "crocodile", "croissant", "crossed_fingers", "crossed_flags", "crossed_swords", "crown", "cry", "crying_cat_face", "crystal_ball", "cuba", "cucumber", "cupid", "curacao", "curly_loop", "currency_exchange", "curry", "custard", "customs", "cyclone", "cyprus", "czech_republic", "dagger", "dancer", "dancers", "dancing_men", "dancing_women", "dango", "dark_sunglasses", "dart", "dash", "date", "de", "deciduous_tree", "deer", "denmark", "department_store", "derelict_house", "desert", "desert_island", "desktop_computer", "detective", "diamond_shape_with_a_dot_inside", "diamonds", "disappointed", "disappointed_relieved", "dizzy", "dizzy_face", "djibouti", "do_not_litter", "dog", "dog2", "doge", "dollar", "dolls", "dolphin", "dominica", "dominican_republic", "door", "doughnut", "dove", "dragon", "dragon_face", "dress", "dromedary_camel", "drooling_face", "droplet", "drum", "duck", "dvd", "e-mail", "eagle", "ear", "ear_of_rice", "earth_africa", "earth_americas", "earth_asia", "ecuador", "egg", "eggplant", "egypt", "eight", "eight_pointed_black_star", "eight_spoked_asterisk", "el_salvador", "electric_plug", "elephant", "email", "end", "envelope", "envelope_with_arrow", "equatorial_guinea", "eritrea", "es", "estonia", "ethiopia", "eu", "euro", "european_castle", "european_post_office", "european_union", "evergreen_tree", "exclamation", "expressionless", "eye", "eye_speech_bubble", "eyeglasses", "eyes", "face_with_head_bandage", "face_with_thermometer", "facepunch", "factory", "falkland_islands", "fallen_leaf", "family", "family_man_boy", "family_man_boy_boy", "family_man_girl", "family_man_girl_boy", "family_man_girl_girl", "family_man_man_boy", "family_man_man_boy_boy", "family_man_man_girl", "family_man_man_girl_boy", "family_man_man_girl_girl", "family_man_woman_boy", "family_man_woman_boy_boy", "family_man_woman_girl", "family_man_woman_girl_boy", "family_man_woman_girl_girl", "family_woman_boy", "family_woman_boy_boy", "family_woman_girl", "family_woman_girl_boy", "family_woman_girl_girl", "family_woman_woman_boy", "family_woman_woman_boy_boy", "family_woman_woman_girl", "family_woman_woman_girl_boy", "family_woman_woman_girl_girl", "faroe_islands", "fast_forward", "fax", "fearful", "feet", "female_detective", "ferris_wheel", "ferry", "field_hockey", "fiji", "file_cabinet", "file_folder", "film_projector", "film_strip", "finland", "fire", "fire_engine", "fireworks", "first_quarter_moon", "first_quarter_moon_with_face", "fish", "fish_cake", "fishing_pole_and_fish", "fist", "fist_left", "fist_oncoming", "fist_raised", "fist_right", "five", "flags", "flashlight", "fleur_de_lis", "flight_arrival", "flight_departure", "flipper", "floppy_disk", "flower_playing_cards", "flushed", "fog", "foggy", "football", "footprints", "fork_and_knife", "fountain", "fountain_pen", "four", "four_leaf_clover", "fox_face", "fr", "framed_picture", "free", "french_guiana", "french_polynesia", "french_southern_territories", "fried_egg", "fried_shrimp", "fries", "frog", "frowning", "frowning_face", "frowning_man", "frowning_woman", "fu", "fuelpump", "full_moon", "full_moon_with_face", "funeral_urn", "gabon", "gambia", "game_die", "gb", "gear", "gem", "gemini", "georgia", "ghana", "ghost", "gibraltar", "gift", "gift_heart", "girl", "globe_with_meridians", "goal_net", "goat", "golf", "golfing_man", "golfing_woman", "gorilla", "grapes", "greece", "green_apple", "green_book", "green_heart", "green_salad", "greenland", "grenada", "grey_exclamation", "grey_question", "grimacing", "grin", "grinning", "guadeloupe", "guam", "guardsman", "guardswoman", "guatemala", "guernsey", "guinea", "guinea_bissau", "guitar", "gun", "guyana", "hacpai", "haircut", "haircut_man", "haircut_woman", "haiti", "hamburger", "hammer", "hammer_and_pick", "hammer_and_wrench", "hamster", "hand", "handbag", "handshake", "hankey", "hash", "hatched_chick", "hatching_chick", "headphones", "hear_no_evil", "heart", "heart_decoration", "heart_eyes", "heart_eyes_cat", "heartbeat", "heartpulse", "hearts", "heavy_check_mark", "heavy_division_sign", "heavy_dollar_sign", "heavy_exclamation_mark", "heavy_heart_exclamation", "heavy_minus_sign", "heavy_multiplication_x", "heavy_plus_sign", "helicopter", "herb", "hibiscus", "high_brightness", "high_heel", "hocho", "hole", "honduras", "honey_pot", "honeybee", "hong_kong", "horse", "horse_racing", "hospital", "hot_pepper", "hotdog", "hotel", "hotsprings", "hourglass", "hourglass_flowing_sand", "house", "house_with_garden", "houses", "huaji", "hugs", "hungary", "hushed", "ice_cream", "ice_hockey", "ice_skate", "icecream", "iceland", "id", "ideograph_advantage", "imp", "inbox_tray", "incoming_envelope", "india", "indonesia", "information_desk_person", "information_source", "innocent", "interrobang", "iphone", "iran", "iraq", "ireland", "isle_of_man", "israel", "it", "izakaya_lantern", "jack_o_lantern", "jamaica", "japan", "japanese_castle", "japanese_goblin", "japanese_ogre", "jeans", "jersey", "jordan", "joy", "joy_cat", "joystick", "jp", "kaaba", "kazakhstan", "kenya", "key", "keyboard", "keycap_ten", "kick_scooter", "kimono", "kiribati", "kiss", "kissing", "kissing_cat", "kissing_closed_eyes", "kissing_heart", "kissing_smiling_eyes", "kiwi_fruit", "knife", "koala", "koko", "kosovo", "kr", "kuwait", "kyrgyzstan", "label", "lantern", "laos", "large_blue_circle", "large_blue_diamond", "large_orange_diamond", "last_quarter_moon", "last_quarter_moon_with_face", "latin_cross", "latke", "latvia", "laughing", "leaves", "lebanon", "ledger", "left_luggage", "left_right_arrow", "leftwards_arrow_with_hook", "lemon", "leo", "leopard", "lesotho", "level_slider", "liberia", "libra", "libya", "liechtenstein", "light_rail", "link", "lion", "lips", "lipstick", "lithuania", "lizard", "lock", "lock_with_ink_pen", "lollipop", "loop", "loud_sound", "loudspeaker", "love_hotel", "love_letter", "low_brightness", "lute", "luxembourg", "lying_face", "m", "macau", "macedonia", "madagascar", "mag", "mag_right", "mahjong", "mailbox", "mailbox_closed", "mailbox_with_mail", "mailbox_with_no_mail", "malawi", "malaysia", "maldives", "male_detective", "mali", "malta", "man", "man_artist", "man_astronaut", "man_cartwheeling", "man_cook", "man_dancing", "man_facepalming", "man_factory_worker", "man_farmer", "man_firefighter", "man_health_worker", "man_in_tuxedo", "man_judge", "man_juggling", "man_mechanic", "man_office_worker", "man_pilot", "man_playing_handball", "man_playing_water_polo", "man_scientist", "man_shrugging", "man_singer", "man_student", "man_teacher", "man_technologist", "man_with_gua_pi_mao", "man_with_turban", "mandarin", "mans_shoe", "mantelpiece_clock", "maple_leaf", "marshall_islands", "martial_arts_uniform", "martinique", "mask", "massage", "massage_man", "massage_woman", "mauritania", "mauritius", "mayotte", "meat_on_bone", "medal_military", "medal_sports", "mega", "melon", "memo", "men_wrestling", "menorah", "mens", "metal", "metro", "mexico", "micronesia", "microphone", "microscope", "middle_finger", "milk_glass", "milky_way", "minibus", "minidisc", "mobile_phone_off", "moldova", "monaco", "money_mouth_face", "money_with_wings", "moneybag", "mongolia", "monkey", "monkey_face", "monorail", "montenegro", "montserrat", "moon", "morocco", "mortar_board", "mosque", "motor_boat", "motor_scooter", "motorcycle", "motorway", "mount_fuji", "mountain", "mountain_bicyclist", "mountain_biking_man", "mountain_biking_woman", "mountain_cableway", "mountain_railway", "mountain_snow", "mouse", "mouse2", "movie_camera", "moyai", "mozambique", "mrs_claus", "muscle", "mushroom", "musical_keyboard", "musical_note", "musical_score", "mute", "myanmar", "nail_care", "name_badge", "namibia", "national_park", "nauru", "nauseated_face", "necktie", "negative_squared_cross_mark", "nepal", "nerd_face", "netherlands", "neutral_face", "new", "new_caledonia", "new_moon", "new_moon_with_face", "new_zealand", "newspaper", "newspaper_roll", "next_track_button", "ng", "ng_man", "ng_woman", "nicaragua", "niger", "nigeria", "night_with_stars", "nine", "niue", "no_bell", "no_bicycles", "no_entry", "no_entry_sign", "no_good", "no_good_man", "no_good_woman", "no_mobile_phones", "no_mouth", "no_pedestrians", "no_smoking", "non-potable_water", "norfolk_island", "north_korea", "northern_mariana_islands", "norway", "nose", "notebook", "notebook_with_decorative_cover", "notes", "nut_and_bolt", "o", "o2", "ocean", "octocat", "octopus", "oden", "office", "oil_drum", "ok", "ok_hand", "ok_man", "ok_woman", "old_key", "older_man", "older_woman", "om", "oman", "on", "oncoming_automobile", "oncoming_bus", "oncoming_police_car", "oncoming_taxi", "one", "open_book", "open_file_folder", "open_hands", "open_mouth", "open_umbrella", "ophiuchus", "orange", "orange_book", "orthodox_cross", "outbox_tray", "owl", "ox", "package", "page_facing_up", "page_with_curl", "pager", "paintbrush", "pakistan", "palau", "palestinian_territories", "palm_tree", "panama", "pancakes", "panda_face", "paperclip", "paperclips", "papua_new_guinea", "paraguay", "parasol_on_ground", "parking", "part_alternation_mark", "partly_sunny", "passenger_ship", "passport_control", "pause_button", "paw_prints", "peace_symbol", "peach", "peanuts", "pear", "pen", "pencil", "pencil2", "penguin", "pensive", "performing_arts", "persevere", "person_fencing", "person_frowning", "person_with_blond_hair", "person_with_pouting_face", "peru", "philippines", "phone", "pick", "pig", "pig2", "pig_nose", "pill", "pineapple", "ping_pong", "pipe", "pisces", "pitcairn_islands", "pizza", "place_of_worship", "plate_with_cutlery", "play_or_pause_button", "point_down", "point_left", "point_right", "point_up", "point_up_2", "poland", "police_car", "policeman", "policewoman", "poodle", "poop", "popcorn", "portugal", "post_office", "postal_horn", "postbox", "potable_water", "potato", "pouch", "poultry_leg", "pound", "pout", "pouting_cat", "pouting_man", "pouting_woman", "pray", "prayer_beads", "pregnant_woman", "previous_track_button", "prince", "princess", "printer", "puerto_rico", "punch", "purple_heart", "purse", "pushpin", "put_litter_in_its_place", "qatar", "question", "rabbit", "rabbit2", "racehorse", "racing_car", "radio", "radio_button", "radioactive", "rage", "railway_car", "railway_track", "rainbow", "rainbow_flag", "raised_back_of_hand", "raised_hand", "raised_hand_with_fingers_splayed", "raised_hands", "raising_hand", "raising_hand_man", "raising_hand_woman", "ram", "ramen", "rat", "record_button", "recycle", "red_car", "red_circle", "registered", "relaxed", "relieved", "reminder_ribbon", "repeat", "repeat_one", "rescue_worker_helmet", "restroom", "reunion", "revolving_hearts", "rewind", "rhinoceros", "ribbon", "rice", "rice_ball", "rice_cracker", "rice_scene", "right_anger_bubble", "ring", "robot", "rocket", "rofl", "roll_eyes", "roller_coaster", "romania", "rooster", "rose", "rosette", "rotating_light", "round_pushpin", "rowboat", "rowing_man", "rowing_woman", "ru", "rugby_football", "runner", "running", "running_man", "running_shirt_with_sash", "running_woman", "rwanda", "sa", "sagittarius", "sailboat", "sake", "samoa", "san_marino", "sandal", "santa", "sao_tome_principe", "sassy_man", "sassy_woman", "satellite", "satisfied", "saudi_arabia", "saxophone", "school", "school_satchel", "scissors", "scorpion", "scorpius", "scream", "scream_cat", "scroll", "seat", "secret", "see_no_evil", "seedling", "selfie", "senegal", "serbia", "seven", "seychelles", "shallow_pan_of_food", "shamrock", "shark", "shaved_ice", "sheep", "shell", "shield", "shinto_shrine", "ship", "shirt", "shit", "shoe", "shopping", "shopping_cart", "shower", "shrimp", "sierra_leone", "signal_strength", "singapore", "sint_maarten", "six", "six_pointed_star", "ski", "skier", "skull", "skull_and_crossbones", "sleeping", "sleeping_bed", "sleepy", "slightly_frowning_face", "slightly_smiling_face", "slot_machine", "slovakia", "slovenia", "small_airplane", "small_blue_diamond", "small_orange_diamond", "small_red_triangle", "small_red_triangle_down", "smile", "smile_cat", "smiley", "smiley_cat", "smiling_imp", "smirk", "smirk_cat", "smoking", "snail", "snake", "sneezing_face", "snowboarder", "snowflake", "snowman", "snowman_with_snow", "sob", "soccer", "solo", "solomon_islands", "somalia", "soon", "sos", "sound", "south_africa", "south_georgia_south_sandwich_islands", "south_sudan", "space_invader", "spades", "spaghetti", "sparkle", "sparkler", "sparkles", "sparkling_heart", "speak_no_evil", "speaker", "speaking_head", "speech_balloon", "speedboat", "spider", "spider_web", "spiral_calendar", "spiral_notepad", "spoon", "squid", "sri_lanka", "st_barthelemy", "st_helena", "st_kitts_nevis", "st_lucia", "st_pierre_miquelon", "st_vincent_grenadines", "stadium", "star", "star2", "star_and_crescent", "star_of_david", "stars", "station", "statue_of_liberty", "steam_locomotive", "stew", "stop_button", "stop_sign", "stopwatch", "straight_ruler", "strawberry", "stuck_out_tongue", "stuck_out_tongue_closed_eyes", "stuck_out_tongue_winking_eye", "studio_microphone", "stuffed_flatbread", "sudan", "sun_behind_large_cloud", "sun_behind_rain_cloud", "sun_behind_small_cloud", "sun_with_face", "sunflower", "sunglasses", "sunny", "sunrise", "sunrise_over_mountains", "surfer", "surfing_man", "surfing_woman", "suriname", "sushi", "suspension_railway", "swaziland", "sweat", "sweat_drops", "sweat_smile", "sweden", "sweet_potato", "swimmer", "swimming_man", "swimming_woman", "switzerland", "sym", "symbols", "synagogue", "syria", "syringe", "taco", "tada", "taiwan", "tajikistan", "tanabata_tree", "tangerine", "tanzania", "taurus", "taxi", "tea", "telephone", "telephone_receiver", "telescope", "tennis", "tent", "thailand", "thermometer", "thinking", "thought_balloon", "three", "thumbsdown", "thumbsup", "ticket", "tickets", "tiger", "tiger2", "timer_clock", "timor_leste", "tipping_hand_man", "tipping_hand_woman", "tired_face", "tm", "togo", "toilet", "tokelau", "tokyo_tower", "tomato", "tonga", "tongue", "top", "tophat", "tornado", "tr", "trackball", "tractor", "traffic_light", "train", "train2", "tram", "triangular_flag_on_post", "triangular_ruler", "trident", "trinidad_tobago", "triumph", "trolleybus", "trollface", "trophy", "tropical_drink", "tropical_fish", "truck", "trumpet", "tshirt", "tulip", "tumbler_glass", "tunisia", "turkey", "turkmenistan", "turks_caicos_islands", "turtle", "tuvalu", "tv", "twisted_rightwards_arrows", "two", "two_hearts", "two_men_holding_hands", "two_women_holding_hands", "u5272", "u5408", "u55b6", "u6307", "u6708", "u6709", "u6e80", "u7121", "u7533", "u7981", "u7a7a", "uganda", "uk", "ukraine", "umbrella", "unamused", "underage", "unicorn", "united_arab_emirates", "unlock", "up", "upside_down_face", "uruguay", "us", "us_virgin_islands", "uzbekistan", "v", "vanuatu", "vatican_city", "vditor", "venezuela", "vertical_traffic_light", "vhs", "vibration_mode", "video_camera", "video_game", "vietnam", "violin", "virgo", "volcano", "volleyball", "vs", "vulcan_salute", "walking", "walking_man", "walking_woman", "wallis_futuna", "waning_crescent_moon", "waning_gibbous_moon", "warning", "wastebasket", "watch", "water_buffalo", "watermelon", "wave", "wavy_dash", "waxing_crescent_moon", "waxing_gibbous_moon", "wc", "weary", "wedding", "weight_lifting_man", "weight_lifting_woman", "western_sahara", "whale", "whale2", "wheel_of_dharma", "wheelchair", "white_check_mark", "white_circle", "white_flag", "white_flower", "white_large_square", "white_medium_small_square", "white_medium_square", "white_small_square", "white_square_button", "wide", "wilted_flower", "wind_chime", "wind_face", "wine_glass", "wink", "wolf", "woman", "woman_artist", "woman_astronaut", "woman_cartwheeling", "woman_cook", "woman_facepalming", "woman_factory_worker", "woman_farmer", "woman_firefighter", "woman_health_worker", "woman_judge", "woman_juggling", "woman_mechanic", "woman_office_worker", "woman_pilot", "woman_playing_handball", "woman_playing_water_polo", "woman_scientist", "woman_shrugging", "woman_singer", "woman_student", "woman_teacher", "woman_technologist", "woman_with_turban", "womans_clothes", "womans_hat", "women_wrestling", "womens", "world_map", "worried", "wrench", "writing_hand", "wulian", "x", "yellow_heart", "yemen", "yen", "yin_yang", "yum", "zambia", "zap", "zero", "zimbabwe", "zipper_mouth_face", "zzz"
    };

    /**
     * Private constructor.
     */
    private Emotions() {
    }
}
