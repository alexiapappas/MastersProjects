QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = A2_QtBucketman
TEMPLATE = app

SOURCES += \
    main.cpp \
    gamescene.cpp \
    bucket.cpp \
    water.cpp \
    cloud.cpp

HEADERS += \
    gamescene.h \
    bucket.h \
    water.h \
    cloud.h

RESOURCES += \
    resources.qrc
