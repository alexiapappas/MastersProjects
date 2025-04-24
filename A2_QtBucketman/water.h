#ifndef WATER_H
#define WATER_H

#include <QObject>
#include <QGraphicsPixmapItem>
#include <QTimer>

class water : public QObject, public QGraphicsPixmapItem {
    Q_OBJECT

public:
    explicit water(QObject *parent = nullptr);

private:
    QTimer *timerDrop;

private slots:
    void moveDroplet();
};

#endif // WATER_H
