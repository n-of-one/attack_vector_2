export enum HackerIcon {
    BEAR = "BEAR",
    BRAIN = "BRAIN",
    BIRD_1 = "BIRD_1",
    CAT = "CAT",
    CRAB = "CRAB",
    DINO_1 = "DINO_1",
    DINO_2 = "DINO_2",
    DRAGON_1 = "DRAGON_1",
    LION = "LION",
    GECKO = "GECKO",
    LIZARD = "LIZARD",
    LIONESS = "LIONESS",
    MONKEY = "MONKEY",
    COBRA = "COBRA",
    LOBSTER_1 = "LOBSTER_1",
    SHARK = "SHARK",
    STINGRAY = "STINGRAY",
    FROG = "FROG",
    BULL = "BULL",
    CROCODILE = "CROCODILE",
    DOG = "DOG",
    DRAGON_2 = "DRAGON_2",
    FISH_1 = "FISH_1",
    HIPPO = "HIPPO",
    HORSE = "HORSE",
    KOALA = "KOALA",
    SEAHORSE = "SEAHORSE",
    SNAKE_2 = "SNAKE_2",
    UNICORN = "UNICORN",
    WOLF = "WOLF",
    TURTLE = "TURTLE",
    MOOSE = "MOOSE",
    CAMEL = "CAMEL",
    EAGLE = "EAGLE",
    DINO_3 = "DINO_3",
    DRAGON_3 = "DRAGON_3",
    ELEPHANT = "ELEPHANT",
    FISH_2 = "FISH_2",
    LOBSTER_2 = "LOBSTER_2",
    CAT_2 = "CAT_2",
    BIRD_2 = "BIRD_2",
    NOT = "NOT",
}

export const hackerIconPath = (type: string, you: boolean) => {
    const fileName = iconFilename[type]
    const theme = "frontier"
    const root = "/img/" + theme + "/actors/hackers/";
    const color = you ? "red/" : "yellow/";
    return root + color + fileName;
}

const iconFilename: { [key: string]: string } = {
    "BEAR": "animal-bear4-sc44.png",
    "BIRD_1": "animal-bird2.png",
    "BRAIN": "brain.png",
    "CAT": "animal-cat3.png",
    "CRAB": "animal-crab2.png",
    "DINO_1": "animal-dinosaur3.png",
    "DINO_2": "animal-dinosaur4.png",
    "DRAGON_1": "animal-dragon1.png",
    "LION": "animal-lion1-sc36.png",
    "GECKO": "animal-lizard2-sc37.png",
    "LIZARD": "animal-lizard1.png",
    "LIONESS": "animal-lion3-sc37.png",
    "MONKEY": "animal-monkey.png",
    "COBRA": "animal-snake1.png",
    "LOBSTER_1": "animal-lobster.png",
    "SHARK": "animal-fish7-sc37.png",
    "STINGRAY": "animal-fish6.png",
    "FROG": "animal-frog.png",
    "BULL": "animal-bull1-sc44.png",
    "CROCODILE": "animal-crocodile-sc43.png",
    "DOG": "animal-dog5-sc44.png",
    "DRAGON_2": "animal-dragon5-sc28.png",
    "FISH_1": "animal-fish.png",
    "HIPPO": "animal-hippo3-sc22.png",
    "HORSE": "animal-horse1.png",
    "KOALA": "animal-koala-bear.png",
    "SEAHORSE": "animal-seahorse2-sc37.png",
    "SNAKE_2": "animal-snake.png",
    "UNICORN": "animal-unicorn.png",
    "WOLF": "animal-wolf-sc44.png",
    "TURTLE": "animal-turtle.png",
    "MOOSE": "animal-moose-sc44.png",
    "CAMEL": "animal-camel2-sc36.png",
    "EAGLE": "animal-bird4-sc44.png",
    "DINO_3": "animal-dinosaur1.png",
    "DRAGON_3": "animal-dragon2.png",
    "ELEPHANT": "animal-elephant1.png",
    "FISH_2": "animal-fish13.png",
    "LOBSTER_2": "animal-lobster1-sc44.png",
    "CAT_2": "animal-cat1.png",
    "BIRD_2": "animal-bird.png",
    "NOT": "animal-cat-print.png"
}