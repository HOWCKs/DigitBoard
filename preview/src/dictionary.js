// Portuguese (PT-BR) and English common word dictionary for auto-complete & suggestions
const DICTIONARY = [
  // DigitBoard & Tech terms
  "DigitBoard", "teclado", "virtual", "Android", "Neumorphism", "AMOLED", "RGB", "Chroma",
  "Gboard", "interface", "desenvolvimento", "configurações", "recursos", "digitação",
  
  // Portuguese Common Words
  "a", "agora", "ainda", "amigo", "amor", "ano", "apenas", "aqui", "assim", "através",
  "bem", "bom", "boa", "brasil", "cada", "caminho", "certo", "coisa", "como", "com",
  "conosco", "contra", "criação", "daqui", "de", "depois", "desde", "dia", "direito",
  "disso", "e", "ela", "ele", "eles", "elas", "em", "enquanto", "então", "entre",
  "equipe", "espaço", "esta", "está", "estou", "estudando", "fazer", "fim", "forma",
  "grande", "grupo", "hoje", "hora", "hoje", "ideia", "imagem", "isso", "já", "junto",
  "lado", "lugar", "mais", "maior", "mas", "meu", "minha", "momento", "muito", "mundo",
  "não", "negócio", "noite", "nome", "nosso", "nova", "novo", "número", "nunca", "o",
  "onde", "ontem", "outra", "outro", "para", "parte", "passado", "pessoa", "pode",
  "porque", "pouco", "primeiro", "qual", "qualquer", "quando", "quanto", "que", "quem",
  "querer", "saber", "se", "sem", "sempre", "sendo", "ser", "seu", "sua", "sim",
  "sistema", "sobre", "tempo", "ter", "trabalho", "tudo", "um", "uma", "usar", "valor",
  "vamos", "vc", "vazio", "veja", "vida", "você", "voltar",

  // English Common Words
  "and", "application", "best", "build", "code", "design", "device", "download",
  "feature", "file", "github", "good", "great", "hello", "help", "input", "keyboard",
  "layout", "like", "main", "method", "mobile", "mode", "new", "online", "open",
  "option", "project", "release", "screen", "search", "select", "service", "settings",
  "smart", "soft", "source", "style", "system", "text", "theme", "time", "user", "view"
];

export function getSuggestions(inputWord) {
  if (!inputWord || inputWord.trim() === '') {
    return ["DigitBoard", "teclado", "Android"];
  }

  const query = inputWord.toLowerCase().trim();
  const matches = DICTIONARY.filter(word => 
    word.toLowerCase().startsWith(query) && word.toLowerCase() !== query
  );

  // If exact match or few startsWith, add fuzzy contains
  if (matches.length < 3) {
    const containsMatches = DICTIONARY.filter(word => 
      word.toLowerCase().includes(query) && 
      !matches.includes(word) && 
      word.toLowerCase() !== query
    );
    matches.push(...containsMatches);
  }

  // Return top 3 unique suggestions
  const result = matches.slice(0, 3);
  if (result.length === 0) {
    return [inputWord, inputWord + "s", inputWord + "ando"];
  }
  return result;
}
