#ifndef CLOUD_H
#define CLOUD_H

#include <QObject>
#include <QGraphicsPixmapItem>
#include <QTimer>

class cloud : public QObject, public QGraphicsPixmapItem {
    Q_OBJECT

public:
    explicit cloud(QObject *parent = nullptr);

private:
    QTimer *moveTimer;
    bool movingRight;

private slots:
    void moveCloud();
};

#endif // CLOUD_H
