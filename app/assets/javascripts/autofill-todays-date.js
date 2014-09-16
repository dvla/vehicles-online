function autoFillTodaysDate(day, month, year, checkboxId, dayId, monthId, yearId) {
    var dod_elm = document.getElementById(checkboxId);
    var dod_day = document.getElementById(dayId);
    var dod_month = document.getElementById(monthId);
    var dod_year = document.getElementById(yearId);

    if (dod_elm.checked === false) {
        dod_day.value = '';
        dod_month.value = '';
        dod_year.value = '';
    } else {
        dod_day.value = day;
        dod_month.value = month;
        dod_year.value = year;
    }
}