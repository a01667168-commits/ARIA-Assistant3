package com.aria.assistant.commands

import com.aria.assistant.commands.handlers.AiFallbackHandler
import com.aria.assistant.commands.handlers.AppCommandHandler
import com.aria.assistant.commands.handlers.CalendarCommandHandler
import com.aria.assistant.commands.handlers.MemoryCommandHandler
import com.aria.assistant.commands.handlers.MusicCommandHandler
import com.aria.assistant.commands.handlers.NotesCommandHandler
import com.aria.assistant.commands.handlers.RemindersCommandHandler
import com.aria.assistant.commands.handlers.SearchCommandHandler
import com.aria.assistant.commands.handlers.SystemCommandHandler
import com.aria.assistant.commands.handlers.TasksCommandHandler
import com.aria.assistant.commands.handlers.WeatherCommandHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Registra cada CommandHandler en un Set<CommandHandler> inyectable
 * (ver CommandEngine). Agregar un dominio de comandos nuevo es crear la
 * clase + un @Binds aquí, sin tocar el resto de la app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommandModule {

    @Binds @IntoSet
    abstract fun bindAppCommandHandler(handler: AppCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindSearchCommandHandler(handler: SearchCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindSystemCommandHandler(handler: SystemCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindNotesCommandHandler(handler: NotesCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindTasksCommandHandler(handler: TasksCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindRemindersCommandHandler(handler: RemindersCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindCalendarCommandHandler(handler: CalendarCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindWeatherCommandHandler(handler: WeatherCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindMusicCommandHandler(handler: MusicCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindMemoryCommandHandler(handler: MemoryCommandHandler): CommandHandler

    @Binds @IntoSet
    abstract fun bindAiFallbackHandler(handler: AiFallbackHandler): CommandHandler
}
