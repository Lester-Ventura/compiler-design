// 26. For-loop and while-loop condition not being a boolean value
rune powerspike : stats = 10;

// this condition would only work if the --lenient flag is set

cannon (item yasuo_deaths: stats = 0; ++yasuo_deaths % 10;) {  
    canwin(yasuo_deaths < powerspike-1) {
        broadcast("Yasuo is scaling!");
    } lose {
        broadcast("GG!");
    }
}
