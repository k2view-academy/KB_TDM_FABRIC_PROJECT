        01  CONTACT-RECORD.
           05 ID                             PIC 9(09).
           05 COMPANYNAME                    PIC X(50).
           05 DESCRIPTION                    PIC X(50).
           05 LATESTACTIVITYDATETIME         PIC X(25).
           05 WEBSITE                        PIC X(50).
           05 GOOGLEWEBTRACKINGID            PIC X(50).
           05 TWITTERID                      PIC X(50).
           05 FACEBOOKID                     PIC X(50).
           05 PERSON_ID                      PIC 9(05).
           05 ACCOUNT_ID                     PIC 9(05).
           05 INDUSTRY_CUSTOMFIELD_ID        PIC 9(04).
           05 SECONDARYADDRESS_ADDRESS_ID    PIC 9(05).
           05 SECONDARYEMAIL_EMAIL_ID        PIC 9(05).
           05 SOURCE_CUSTOMFIELD_ID          PIC 9(04).
           05 STATE_CONTACTSTATE_ID          PIC 9(04).
           05 CUSTOM-ATTR.
              10 STATUSCSTM                  PIC X(10).
              10 CREDITCARDCSTM              PIC X(25).
              10 SSNCSTM                     PIC X(10).
              10 CONTACT_IDCSTM              PIC 9(09).
           05 PERSON-RECORD.
              10 PERSON-ID                   PIC 9(05).
              10 DEPARTMENT                  PIC X(50).
              10 NAME.
                 20 FIRSTNAME                PIC X(50).
                 20 LASTNAME                 PIC X(50).
              10 JOBTITLE                    PIC X(50).
              10 PHONE-INFO.
                 20 MOBILEPHONE              PIC X(50).
                 20 OFFICEPHONE              PIC X(50).
                 20 OFFICEFAX                PIC X(50).
              10 OWNEDSECURABLEITEM_ID       PIC 9(05).
              10 PRIMARYADDRESS_ADDRESS_ID   PIC 9(05).
              10 PRIMARYEMAIL_EMAIL_ID       PIC 9(05).
              10 TITLE_CUSTOMFIELD_ID        PIC 9(05).
           05 ADDRESS-RECORD.
              10 ADDRESS-ID                  PIC 9(05).
              10 STREET1                     PIC X(50).
              10 STREET2                     PIC X(50).
              10 CITY                        PIC X(50).
              10 STATE                       PIC X(50).
              10 COUNTRY                     PIC X(50).
              10 POSTALCODE                  PIC X(50).
              10 LATITUDE                    PIC X(50).
              10 LONGITUDE                   PIC X(50).
              10 INVALID                     PIC X(50).
           05 EMAIL-RECORD.
              10 EMAIL-ID                    PIC 9(09).
              10 EMAILADDRESS                PIC X(50).
              10 ISINVALID                   PIC X(50).
              10 OPTOUT                      PIC X(50).