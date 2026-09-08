package com.digitboard.keyboard.service

object SuggestionEngine {
    private val dictionary = listOf(
        "DigitBoard", "teclado", "virtual", "Android", "Neumorphism", "AMOLED", "RGB",
        "Gboard", "interface", "desenvolvimento", "configurações", "recursos", "digitação",
        "agora", "ainda", "amigo", "amor", "apenas", "aqui", "assim", "através",
        "bem", "bom", "boa", "brasil", "caminho", "certo", "coisa", "como", "com",
        "conosco", "contra", "criação", "depois", "desde", "dia", "direito", "disso",
        "ele", "ela", "eles", "elas", "em", "enquanto", "então", "entre", "equipe",
        "espaço", "esta", "está", "estou", "fazer", "fim", "forma", "grande", "grupo",
        "hoje", "hora", "ideia", "imagem", "isso", "junto", "lado", "lugar", "mais",
        "maior", "mas", "meu", "minha", "momento", "muito", "mundo", "não", "nome",
        "nosso", "nova", "novo", "número", "nunca", "onde", "ontem", "outra", "outro",
        "para", "parte", "passado", "pessoa", "pode", "porque", "pouco", "primeiro",
        "qual", "quando", "quanto", "que", "quem", "querer", "saber", "se", "sem",
        "sempre", "ser", "seu", "sua", "sim", "sistema", "sobre", "tempo", "ter",
        "trabalho", "tudo", "um", "uma", "usar", "valor", "vamos", "você", "voltar"
    )

    fun getSuggestions(input: String): List<String> {
        if (input.isBlank()) return listOf("DigitBoard", "teclado", "Android")
        val query = input.lowercase().trim()
        val matches = dictionary.filter { it.lowercase().startsWith(query) && it.lowercase() != query }
            .toMutableList()

        if (matches.size < 3) {
            val contains = dictionary.filter { it.lowercase().contains(query) && !matches.contains(it) && it.lowercase() != query }
            matches.addAll(contains)
        }

        return if (matches.isNotEmpty()) matches.take(3) else listOf(input, "${input}s", "${input}ando")
    }
}
