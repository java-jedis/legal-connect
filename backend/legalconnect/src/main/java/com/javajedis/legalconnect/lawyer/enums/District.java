package com.javajedis.legalconnect.lawyer.enums;

public enum District {
    BAGERHAT("Bagerhat"),
    BANDARBAN("Bandarban"),
    BARGUNA("Barguna"),
    BARISHAL("Barishal"),
    BHOLA("Bhola"),
    BOGRA("Bogra"),
    BRAHMANBARIA("Brahmanbaria"),
    CHANDPUR("Chandpur"),
    CHAPAI_NAWABGANJ("Chapai Nawabganj"),
    CHATTOGRAM("Chattogram"),
    CHUADANGA("Chuadanga"),
    COMILLA("Comilla"),
    COXS_BAZAR("Cox's Bazar"),
    DHAKA("Dhaka"),
    DINAJPUR("Dinajpur"),
    FARIDPUR("Faridpur"),
    FENI("Feni"),
    GAIBANDHA("Gaibandha"),
    GAZIPUR("Gazipur"),
    GOPALGANJ("Gopalganj"),
    HABIGANJ("Habiganj"),
    JAMALPUR("Jamalpur"),
    JASHORE("Jashore"),
    JHALOKATHI("Jhalokathi"),
    JHENAIDAH("Jhenaidah"),
    JOYPURHAT("Joypurhat"),
    KHAGRACHHARI("Khagrachhari"),
    KHULNA("Khulna"),
    KISHOREGANJ("Kishoreganj"),
    KURIGRAM("Kurigram"),
    KUSHTIA("Kushtia"),
    LAKSHMIPUR("Lakshmipur"),
    LALMONIRHAT("Lalmonirhat"),
    MADARIPUR("Madaripur"),
    MAGURA("Magura"),
    MANIKGANJ("Manikganj"),
    MEHERPUR("Meherpur"),
    MOULVIBAZAR("Moulvibazar"),
    MUNSHIGANJ("Munshiganj"),
    MYMENSINGH("Mymensingh"),
    NAOGAON("Naogaon"),
    NARAIL("Narail"),
    NARAYANGANJ("Narayanganj"),
    NARSINGDI("Narsingdi"),
    NATORE("Natore"),
    NETROKONA("Netrokona"),
    NILPHAMARI("Nilphamari"),
    NOAKHALI("Noakhali"),
    PABNA("Pabna"),
    PANCHAGARH("Panchagarh"),
    PATUAKHALI("Patuakhali"),
    PIROJPUR("Pirojpur"),
    RAJBARI("Rajbari"),
    RAJSHAHI("Rajshahi"),
    RANGAMATI("Rangamati"),
    RANGPUR("Rangpur"),
    SATKHIRA("Satkhira"),
    SHARIATPUR("Shariatpur"),
    SHERPUR("Sherpur"),
    SIRAJGANJ("Sirajganj"),
    SUNAMGANJ("Sunamganj"),
    SYLHET("Sylhet"),
    TANGAIL("Tangail"),
    THAKURGAON("Thakurgaon");

    private final String displayName;

    District(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 